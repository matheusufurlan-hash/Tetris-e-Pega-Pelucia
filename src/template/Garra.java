package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.image.Image;
import br.com.davidbuzatto.jsge.math.MathUtils;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.util.Random;

/**
 * @author Yoná Maia & Matheus Ubinha
 */

public class Garra {

    public enum Estado { PARADA, DESCENDO, FECHANDO, SUBINDO }

    private static final double VELOCIDADE_X = 280;
    private static final double VELOCIDADE_Y = 320;
    private static final double TEMPO_FECHAR = 0.4;

    // imagens na escala 3x; as distâncias vêm do LEIA-ME da arte
    private static final String PASTA_IMAGENS = "src/3x/garra/";
    private static final int QUADRO_ABERTA = 0;      // garra_0: totalmente aberta (1 e 2 = fechando)
    private static final int QUADRO_SEGURANDO = 3;   // garra_3: fechada em volta de uma pelúcia
    private static final int QUADRO_FECHADA = 4;     // garra_4: fechou sem nada (errou)
    private static final double TEMPO_QUADRO_ABRINDO = 0.06;   // segundos de cada quadro ao abrir
    private static final double TEMPO_ENCAIXE = 0.15;          // a pelúcia agarrada desliza até o meio da garra

    public static final double DISTANCIA_PELUCIA = 78;   // do topo da garra até o centro da pelúcia presa
    private static final double AJUSTE_CARRINHO = -12;   // topo do carrinho, medido a partir do topo do trilho
    private static final double AJUSTE_CABO = 27;        // onde o cabo sai do carrinho, medido a partir do topo do trilho

    private final double forca;
    private final Random random;


    private final Vector2 posicao;   // x = centro da garra, y = topo da imagem da garra
    private final double yTrilho;    // topo do trilho, onde o carrinho corre
    private final double yRepouso;   // altura da garra parada
    private final double yFundo;     // profundidade máxima da descida
    private final double xMin;
    private final double xMax;

    private Estado estado = Estado.PARADA;
    private double yAlvo;            // profundidade da descida atual
    private double tempoFechando;
    private boolean momentoDeAgarrar;
    private Pelucia carregando;

    // animação: só muda o desenho, a lógica continua a mesma
    private Image[] quadros;         // garra_0 ... garra_4
    private Image carrinho;
    private Image cabo;
    private boolean tentouCarregar;
    private int quadroAbrindo = QUADRO_ABERTA;   // quadro enquanto abre (QUADRO_ABERTA = já abriu)
    private double tempoAbrindo;
    private double encaixeX;         // distância da pelúcia até o meio da garra na hora de agarrar
    private double encaixeY;
    private double tempoEncaixe;

    public Garra( double forca, Random random, double xInicial, double xMin, double xMax, double yTrilho, double yRepouso, double yFundo ) {

        this.forca = forca;
        this.random = random;
        this.xMin = xMin;
        this.xMax = xMax;
        this.yTrilho = yTrilho;
        this.yRepouso = yRepouso;
        this.yFundo = yFundo;
        this.yAlvo = yFundo;
        this.posicao = new Vector2( xInicial, yRepouso );
    }


    public double chanceDePegar( Pelucia pelucia ) {

        double chance = forca - pelucia.getTipo().getPeso();
        return MathUtils.clamp( chance, 0.05, 0.95 );
    }

    public boolean tentarPegar( Pelucia pelucia ) {

        return random.nextDouble() < chanceDePegar( pelucia );
    }


    public void mover( int direcao, double delta ) {

        if ( estado == Estado.PARADA ) {
            posicao.x = MathUtils.clamp( posicao.x + direcao * VELOCIDADE_X * delta, xMin, xMax );
        }
    }


    public void alinharX( double x ) { //centraliza na pilha

        if ( estado == Estado.PARADA ) {
            posicao.x = MathUtils.clamp( x, xMin, xMax );
        }
    }

    public boolean iniciarDescida() {
        return iniciarDescida( yFundo ); //profundidade máxima

    }


    public boolean iniciarDescida( double yDestino ) {
        if ( estado != Estado.PARADA ) {
            return false;
        }

        yAlvo = MathUtils.clamp( yDestino, yRepouso, yFundo );
        estado = Estado.DESCENDO;
        return true;
    }


    public void atualizar( double delta ) {

        switch ( estado ) {
            case DESCENDO -> {
                posicao.y += VELOCIDADE_Y * delta;
                if ( posicao.y >= yAlvo ) { // chegou na profundidade escolhida: começa a fechar

                    posicao.y = yAlvo;
                    estado = Estado.FECHANDO;
                    tempoFechando = 0;
                }
            }
            case FECHANDO -> {
                tempoFechando += delta;
                if ( tempoFechando >= TEMPO_FECHAR ) {
                    // terminou de fechar: avisa (uma vez) que é hora de tentar agarrar
                    momentoDeAgarrar = true;
                    estado = Estado.SUBINDO;
                }
            }
            case SUBINDO -> {
                posicao.y -= VELOCIDADE_Y * delta;
                if ( posicao.y <= yRepouso ) {
                    posicao.y = yRepouso;
                    estado = Estado.PARADA;
                    if ( carregando == null ) {
                        comecarAbrir( QUADRO_FECHADA ); // voltou vazia: abre de novo
                    }
                }
            }
            default -> { }
        }

        // abrindo: a cada TEMPO_QUADRO_ABRINDO segundos a garra volta um quadro, até ficar aberta
        if ( quadroAbrindo > QUADRO_ABERTA ) {
            tempoAbrindo += delta;
            if ( tempoAbrindo >= TEMPO_QUADRO_ABRINDO ) {
                tempoAbrindo = 0;
                quadroAbrindo--;
            }
        }

        // a pelúcia segurada acompanha a garra
        if ( carregando != null ) {
            tempoEncaixe -= delta;
            if ( tempoEncaixe < 0 ) {
                tempoEncaixe = 0;
            }
            acompanhar();
        }

    }

    /** Devolve true uma única vez, quando a garra terminou de fechar no fundo. */
    public boolean momentoAgarrar() {
        boolean r = momentoDeAgarrar;
        momentoDeAgarrar = false;
        return r;
    }

    /** Passa a segurar a pelúcia. */
    public void carregar( Pelucia pelucia ) {
        this.carregando = pelucia;
        // em vez de "pular" para o meio da garra, a pelúcia desliza até lá a partir de onde estava
        encaixeX = pelucia.getPosicao().x - posicao.x;
        encaixeY = pelucia.getPosicao().y - ( posicao.y + DISTANCIA_PELUCIA );
        tempoEncaixe = TEMPO_ENCAIXE;
        acompanhar();
    }

    /** Solta o que está carregando (e devolve a pelúcia). */
    public Pelucia soltar() {
        Pelucia p = carregando;
        carregando = null;
        if ( p != null ) {
            comecarAbrir( QUADRO_SEGURANDO );
        }
        return p;
    }

    // põe a pelúcia segurada no meio da garra. Logo depois de agarrar ela ainda está
    // um pouco fora do meio (encaixeX, encaixeY) e vai chegando lá aos poucos
    private void acompanhar() {
        double falta = tempoEncaixe / TEMPO_ENCAIXE;   // 1 = acabou de agarrar, 0 = já encaixou
        double x = posicao.x + encaixeX * falta;
        double y = posicao.y + DISTANCIA_PELUCIA + encaixeY * falta;
        carregando.moverPara( x, y );
    }

    // --- Animação ---

    private void comecarAbrir( int quadroInicial ) {
        quadroAbrindo = quadroInicial;
        tempoAbrindo = 0;
    }

    /** Escolhe o quadro da garra de acordo com o que ela está fazendo. */
    private int quadroAtual() {

        if ( carregando != null ) {
            return QUADRO_SEGURANDO;
        }

        if ( estado == Estado.FECHANDO ) {
            // o tempo de fechar é dividido em 3 partes: quadros 0, 1 e 2
            int quadro = (int) ( tempoFechando / ( TEMPO_FECHAR / 3 ) );
            if ( quadro > 2 ) {
                quadro = 2;
            }
            return quadro;
        }

        if ( estado == Estado.SUBINDO ) {   // subindo vazia: fechou sem pegar nada
            return QUADRO_FECHADA;
        }

        return quadroAbrindo;   // ainda abrindo, ou QUADRO_ABERTA quando já abriu

    }

    // --- Desenho ---

    /** Carrega as imagens na primeira vez (precisa da engine para o loadImage). */
    private void carregarImagens( EngineFrame e ) {

        if ( tentouCarregar ) {
            return;
        }
        tentouCarregar = true;

        quadros = new Image[5];
        for ( int i = 0; i < quadros.length; i++ ) {
            quadros[i] = e.loadImage( PASTA_IMAGENS + "garra_" + i + ".png" );
        }
        carrinho = e.loadImage( PASTA_IMAGENS + "garra_carrinho.png" );
        cabo = e.loadImage( PASTA_IMAGENS + "garra_cabo.png" );

    }

    public void desenhar( EngineFrame e ) {

        carregarImagens( e );

        double x = posicao.x;
        double y = posicao.y;

        // carrinho apoiado no trilho
        e.drawImage( carrinho, x - carrinho.getWidth() / 2.0, yTrilho + AJUSTE_CARRINHO );

        // cabo: repete o pedaço de cabo do carrinho até o topo da garra
        double yCabo = yTrilho + AJUSTE_CABO;
        while ( yCabo < y ) {
            e.drawImage( cabo, x - cabo.getWidth() / 2.0, yCabo );
            yCabo += cabo.getHeight();
        }

        // pelúcia segurada fica atrás dos dedos (e sem sombra, porque está no ar)
        if ( carregando != null ) {
            Vector2 posicaoPelucia = carregando.getPosicao();
            carregando.getTipo().desenhar( e, posicaoPelucia.x, posicaoPelucia.y, Pelucia.Raio );
        }

        Image imagem = quadros[quadroAtual()];
        e.drawImage( imagem, x - imagem.getWidth() / 2.0, y );

    }

    public double getX() { return posicao.x; }
    public Estado getEstado() { return estado; }
    public Pelucia getCarregando() { return carregando; }
    public double getForca() { return forca; }

}
