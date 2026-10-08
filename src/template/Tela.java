package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.image.Image;
import br.com.davidbuzatto.jsge.math.MathUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * @author Yoná Maia & Matheus Ubinha
 */


public class Tela extends EngineFrame {

    private static final int LARGURA = 768;
    private static final int ALTURA = 672;

    // arte da máquina (escala 3x): o fundo vai atrás de tudo e o vidro na frente de tudo
    private static final String CAMINHO_FUNDO_TITULO = "src/3x/maquina/maquina_fundo.png";      // letreiro escrito (tela inicial)
    private static final String CAMINHO_FUNDO = "src/3x/maquina/maquina_fundo_sem_texto.png";   // letreiro vazio vira o placar
    private static final String CAMINHO_VIDRO = "src/3x/maquina/maquina_vidro.png";

    // letreiro do topo (a parte escura vai de x 78 a 690 e de y 24 a 96)

    private static final double INFO_X = 96, INFO_Y = 30, INFO_L = 160;
    private static final double ORDEM_X = 276, ORDEM_Y = 60, ORDEM_L = 410;

    // avisos

    private static final double AVISO_X = 538, AVISO_Y = 27, AVISO_L = 146, AVISO_A = 66;
    private static final double AVISO_DURACAO = 2.0;

    private static final double VIDRO_Y = 132;      // topo do vidro
    private static final double TRILHO_Y = 150;     // topo do trilho
    private static final double CHAO_Y = 516;       // nenhuma pelúcia passa daqui

    private static final double LINHA_FRENTE = CHAO_Y - Pelucia.Raio;
    private static final double LINHA_TRAS = LINHA_FRENTE - 42;

    private static final double X_MIN_PELUCIAS = 288, X_MAX_PELUCIAS = 616;

    private static final double Y_REPOUSO = TRILHO_Y + 30;     // cabo recolhido
    private static final double DESCIDA = Garra.DISTANCIA_PELUCIA;   // do topo da garra até o centro da pelúcia que ela pega
    private static final double Y_FUNDO = LINHA_FRENTE - DESCIDA;
    private static final double GARRA_X_MIN = 174;   // centro da caixa: toda para a esquerda, a garra fica bem em cima dela
    private static final double GARRA_X_MAX = 615;   // aberta, a garra não atravessa o vidro da direita

    // pilha é a caixa escura a esquerda

    private static final double ZONA_X = 108, ZONA_L = 132;
    private static final double ALTURA_ANDAR = 55;


    private static final double PILHA_Y_BASE = CHAO_Y - Pelucia.Raio + ALTURA_ANDAR / 2.0;

    private static final double TEMPO_SUMIR = 0.3;   // pelúcia retirada da pilha encolhe até sumir

    private Controle jogo;
    private Garra garra;
    private ZonaDeEntrega zona;
    private List<Pelucia> naMaquina;        // pelúcias soltas na máquina
    private Pelucia peluciaLargada;         // pelúcia que a garra acabou de soltar e ainda está caindo
    private boolean desempilhando;          // garra subindo com uma pelúcia retirada da pilha
    private boolean comecou;
    private Image fundo;                    // letreiro vazio (placar)
    private Image fundoTitulo;              // letreiro com "PEGA PELÚCIA" (tela inicial)
    private Image vidro;                    // reflexo do vidro e frente da caixa do prêmio

    private Pelucia sumindo;                // pelúcia que saiu da pilha (só animação)
    private double tempoSumindo;

    private String[] aviso;
    private double tempoAviso;
    private Color corAviso = Color.WHITE;

    public Tela() {
        super( LARGURA, ALTURA, "Pega Pelúcia - Pilha", 60, true );
    }

    @Override
    public void create() {
        setDefaultFontSize( 16 );
        naMaquina = new ArrayList<>();
        carregarImagens();
        prepararPartida();
        comecou = false;
    }

    private void carregarImagens() {
        fundoTitulo = loadImage( CAMINHO_FUNDO_TITULO );
        fundo = loadImage( CAMINHO_FUNDO );
        vidro = loadImage( CAMINHO_VIDRO );
    }

    private void prepararPartida() {
        zona = new ZonaDeEntrega( ZONA_X, VIDRO_Y, ZONA_L, CHAO_Y - VIDRO_Y );
        garra = new Garra( 0.9, new Random(), 400, GARRA_X_MIN, GARRA_X_MAX, TRILHO_Y, Y_REPOUSO, Y_FUNDO );
        jogo = new Controle( garra, zona );
        peluciaLargada = null;
        desempilhando = false;
        sumindo = null;
        tempoAviso = 0;
        popularMaquina();
    }

    private void popularMaquina() { //populariza a máquina coloca 2 de cada animalzinho

        naMaquina.clear();
        List<TipoPelucia> tipos = new ArrayList<>();

        for ( TipoPelucia t : TipoPelucia.values() ) {

            for ( int k = 0; k < SequenciaAlvo.MAX_POR_TIPO; k++ ) {
                tipos.add( t );
            }
        }

        Collections.shuffle( tipos );

        // intercaladas: cada pelúcia de trás aparece entre duas da frente
        double passo = ( X_MAX_PELUCIAS - X_MIN_PELUCIAS ) / ( tipos.size() - 1 );

        for ( int i = 0; i < tipos.size(); i++ ) {

            boolean frente = i % 2 == 0;
            double x = X_MIN_PELUCIAS + i * passo + MathUtils.getRandomValue( -4, 4 );

            naMaquina.add( new Pelucia( tipos.get( i ), x, frente ? LINHA_FRENTE : LINHA_TRAS ) );
        }

    }

    private double centroDoAndar( int i ) {
        return PILHA_Y_BASE - ( i + 0.5 ) * ALTURA_ANDAR;
    }

    private double sortearChao() {
        return MathUtils.getRandomValue( 0, 1 ) == 0 ? LINHA_FRENTE : LINHA_TRAS;

    }

    @Override
    public void update( double delta ) {

        if ( tempoAviso > 0 ) {
            tempoAviso -= delta;
        }
        if ( tempoSumindo > 0 ) {
            tempoSumindo -= delta;
        }

        for ( Pelucia p : new ArrayList<>( naMaquina ) ) { // as pelúcias caem mesmo na tela inicial
            p.atualizar( delta );

            if ( p.consumirPouso() && p == peluciaLargada ) {
                tratarPouso( p );

            }
        }

        if ( !comecou || jogo.getFase() == Controle.Fase.FIM_DE_JOGO
                      || jogo.getFase() == Controle.Fase.VITORIA ) {

            if ( isKeyPressed( KEY_ENTER ) ) {
                prepararPartida();
                jogo.iniciar();
                comecou = true;

            }
            return;
        }

        if ( isKeyDown( KEY_LEFT ) || isKeyDown( KEY_A ) ) {
            garra.mover( -1, delta );
        }
        if ( isKeyDown( KEY_RIGHT ) || isKeyDown( KEY_D ) ) {
            garra.mover( 1, delta );
        }
        if ( isKeyPressed( KEY_SPACE ) ) {
            acaoDoEspaco();
        }

        jogo.atualizar( delta );
        garra.atualizar( delta );
        atualizarGarra();

    }

    private void acaoDoEspaco() {

        if ( garra.getEstado() != Garra.Estado.PARADA || desempilhando ) {
            return;
        }

        boolean sobrePilha = zona.reconheceColuna( garra.getX() );

        if ( garra.getCarregando() != null ) {
            soltarPelucia( sobrePilha );
            return;
        }

        if ( jogo.getFase() == Controle.Fase.EMPILHANDO ) {

            if ( !sobrePilha ) {
                garra.iniciarDescida();

            }

        } else if ( jogo.getFase() == Controle.Fase.DESEMPILHANDO ) {

            if ( sobrePilha ) {
                garra.alinharX( zona.getCentroX() );

                double yTopo = centroDoAndar( jogo.getTamanhoPilha() - 1 );
                garra.iniciarDescida( yTopo - DESCIDA );

            }

        }

    }

    private void soltarPelucia( boolean sobrePilha ) {

        Pelucia p = garra.soltar();

        if ( sobrePilha ) {
            p.moverPara( zona.getCentroX(), p.getPosicao().y );
            p.soltar( centroDoAndar( jogo.getTamanhoPilha() ) );

        } else {
            p.soltar( sortearChao() );
        }

        naMaquina.add( p );
        peluciaLargada = p;

    }

    private void atualizarGarra() {

        if ( garra.momentoAgarrar() ) {

            if ( jogo.getFase() == Controle.Fase.EMPILHANDO ) {

                Pelucia alvo = peluciaProx();

                if ( alvo == null ) {
                    avisar( EngineFrame.ORANGE, "NADA AQUI", "Alinhe a garra" );

                } else if ( jogo.tentarPegar( alvo ) ) {
                    naMaquina.remove( alvo );
                    garra.carregar( alvo );

                } else {
                    avisar( EngineFrame.ORANGE, "ESCORREGOU!", alvo.getTipo().getNome() + " escapou" );
                }

            } else if ( jogo.getFase() == Controle.Fase.DESEMPILHANDO ) {

                if ( zona.reconheceColuna( garra.getX() ) ) {
                    int rodadaAntes = jogo.getRodada();
                    Pelucia p = jogo.tentarDesempilhar();

                    if ( p != null ) {
                        garra.carregar( p );
                        desempilhando = true;

                        if ( jogo.getRodada() != rodadaAntes ) {
                            popularMaquina(); // reabastece a mquina
                        }
                    } else {
                        avisar( EngineFrame.ORANGE, "ESCORREGOU!", "Tente de novo" );

                    }
                }

            }

        }

        if ( desempilhando && garra.getEstado() == Garra.Estado.PARADA ) {
            sumindo = garra.soltar();   // a pelúcia sai da máquina: encolhe até sumir
            tempoSumindo = TEMPO_SUMIR;
            desempilhando = false;

        }

    }


    private Pelucia peluciaProx() {
        Pelucia melhor = null;
        double melhorDist = 38;

        for ( Pelucia p : naMaquina ) {
            double d = Math.abs( p.getPosicao().x - garra.getX() );

            if ( !p.isCaindo() && ( d < melhorDist || ( melhor != null && d == melhorDist && p.getPosicao().y > melhor.getPosicao().y ) ) ) {
                melhor = p;
                melhorDist = d;
            }
        }
        return melhor;
    }

    private void tratarPouso( Pelucia p ) { //verifica se é a correta

        peluciaLargada = null;

        TipoPelucia esperado = null;

        if ( jogo.getFase() == Controle.Fase.EMPILHANDO && !jogo.getSequencia().concluida() ) {
            esperado = jogo.getSequencia().getEsperado();

        }

        switch ( jogo.soltar( p.getPosicao().x, p.getPosicao().y ) ) {
            case EMPILHADA -> {

                naMaquina.remove( p );
                avisar( EngineFrame.GREEN, "CERTO!", "+" + (int) Controle.BONUS_TEMPO + " segundos" );
            }
            case SEQUENCIA_COMPLETA -> {
                naMaquina.remove( p );
                avisar( EngineFrame.GOLD, "SEQUÊNCIA", "COMPLETA!", "Desempilhe!" );
            }
            case ORDEM_ERRADA -> {
                devolverParaMaquina( p );
                avisar( EngineFrame.RED, "PELÚCIA", "ERRADA!", "Era: " + ( esperado != null ? esperado.getNome() : "outra" ) );
            }
            case FORA_DA_ZONA -> avisar( EngineFrame.ORANGE, "FORA DA PILHA", "Solte na coluna" );
            default -> { }
        }

    }

    private void devolverParaMaquina( Pelucia p ) { // reaparece na altura da garra parada e cai
        p.moverPara( MathUtils.getRandomValue( (int) X_MIN_PELUCIAS, (int) X_MAX_PELUCIAS ), Y_REPOUSO + DESCIDA );
        p.soltar( sortearChao() );
    }

    private void avisar( Color cor, String... linhas ) {
        aviso = linhas;
        corAviso = cor;
        tempoAviso = AVISO_DURACAO;
    }

    @Override
    public void draw() {

        // ordem da arte: fundo, pelúcias, pilha, garra e, por último, o vidro
        if ( comecou ) {
            drawImage( fundo, 0, 0 );
        } else {
            drawImage( fundoTitulo, 0, 0 );   // na tela inicial o letreiro mostra o nome do jogo
        }
        setStrokeLineWidth( 2f );

        List<Pelucia> ordenadas = new ArrayList<>( naMaquina );
        ordenadas.sort( Comparator.comparingDouble( p -> p.getPosicao().y ) );

        for ( Pelucia p : ordenadas ) {
            p.desenhar( this );

        }

        jogo.desenharPilha( this, zona.getCentroX(), PILHA_Y_BASE, ALTURA_ANDAR );
        garra.desenhar( this );
        desenharSumindo();
        drawImage( vidro, 0, 0 );   // a frente da caixa cobre a pilha: parece que as pelúcias estão dentro dela

        if ( comecou ) {
            jogo.desenharHud( this, INFO_X, INFO_Y, INFO_L, ORDEM_X, ORDEM_Y, ORDEM_L );
        }
        desenharAviso();

        if ( !comecou ) {
            desenharTelaCheia( "PEGA PELÚCIA", "Pressione ENTER para começar", "Setas: mover a garra   Espaço: descer / soltar" );

        } else if ( jogo.getFase() == Controle.Fase.FIM_DE_JOGO ) {
            desenharTelaCheia( "FIM DE JOGO", "Pontos: " + jogo.getPontos() + "   (rodada " + jogo.getRodada() + ")",
                    "Pressione ENTER para jogar de novo" );

        } else if ( jogo.getFase() == Controle.Fase.VITORIA ) {
            desenharTelaCheia( "VOCÊ VENCEU!", "Pontos: " + jogo.getPontos() + "   Erros: " + jogo.getErros(),
                    "Pressione ENTER para jogar de novo" );
        }

    }

    // pelúcia retirada da pilha: encolhe e sobe um pouco até sumir
    private void desenharSumindo() {

        if ( sumindo == null || tempoSumindo <= 0 ) {
            return;
        }

        double fracao = tempoSumindo / TEMPO_SUMIR;   // vai de 1 até 0
        double raio = Pelucia.Raio * fracao;
        double subida = ( 1 - fracao ) * 30;
        sumindo.getTipo().desenhar( this, sumindo.getPosicao().x, sumindo.getPosicao().y - subida, raio );

    }

    private void desenharAviso() {

        if ( tempoAviso <= 0 || aviso == null ) {
            return;
        }

        int alfa = (int) ( 255 * Math.min( 1.0, tempoAviso / 0.4 ) );
        Color cor = new Color( corAviso.getRed(), corAviso.getGreen(), corAviso.getBlue(), alfa );

        fillRoundRectangle( AVISO_X, AVISO_Y, AVISO_L, AVISO_A, 0.15, new Color( 20, 15, 50, Math.min( 225, alfa ) ) );
        setStrokeLineWidth( 3f );

        drawRoundRectangle( AVISO_X, AVISO_Y, AVISO_L, AVISO_A, 0.15, cor );
        setStrokeLineWidth( 2f );

        int n = aviso.length;
        double altura = ( n - 1 ) * 21 + 16;
        double y = AVISO_Y + ( AVISO_A - altura ) / 2.0;

        for ( int i = 0; i < n; i++ ) {

            boolean titulo = i < n - 1 || n == 1;
            int tam = titulo ? 17 : 13;

            setFontSize( tam );


            while ( tam > 9 && measureText( aviso[i] ) > AVISO_L - 12 ) {
                tam--;
                setFontSize( tam );
            }

            double x = AVISO_X + ( AVISO_L - measureText( aviso[i] ) ) / 2.0;

            drawText( aviso[i], x, y, titulo ? cor : new Color( 255, 255, 255, alfa ) );
            y += titulo ? 21 : 16;
        }

    }

    private void desenharTelaCheia( String titulo, String linha1, String linha2 ) {

        fillRectangle( 0, 0, LARGURA, ALTURA, new Color( 0, 0, 0, 170 ) );

        setFontSize( 36 );
        drawText( titulo, ( LARGURA - measureText( titulo ) ) / 2.0, 250, GOLD );

        setFontSize( 22 );
        drawText( linha1, ( LARGURA - measureText( linha1 ) ) / 2.0, 320, WHITE );

        if ( linha2 != null ) {
            setFontSize( 18 );
            drawText( linha2, ( LARGURA - measureText( linha2 ) ) / 2.0, 365, SKYBLUE );
        }

    }


    public static void main( String[] args ) {
        new Tela();
    }

}
