package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import java.awt.Color;
import java.util.Random;

/**
 * Menu inicial: escolhe entre o Tetris (fila) e o Pega Pelúcia (pilha).
 *
 * Cada jogo continua sendo uma janela da JSGE independente (o Tetris não foi
 * alterado). O menu só abre a janela do jogo escolhido e, quando ela é fechada
 * (no X ou com ESC), volta a aparecer.
 *
 * @author Yoná Maia & Matheus Ubinha
 */
public class MenuInicial extends EngineFrame {

    private static final int LARGURA = 800;
    private static final int ALTURA = 450;

    private static final double CARTAO_Y = 92;
    private static final double ENTRE_CARTOES = 40;
    private static final int QUANTIDADE_ESTRELAS = 40;

    // logo que o menu aparece o ESC é ignorado: assim quem saiu de um jogo
    // segurando o ESC não fecha o programa sem querer
    private static final double TEMPO_PROTECAO_ESC = 0.5;

    private static final Color FUNDO = new Color( 29, 19, 54 );
    private static final Color COR_TEXTO_FRACO = new Color( 190, 180, 230 );
    private static final Color COR_DICA = new Color( 96, 84, 150 );

    // os campos são preparados no create(): a JSGE chama o create() antes dos inicializadores da subclasse
    private Cartao[] cartoes;
    private Estrela[] estrelas;
    private int escolhido;          // posição do cartão escolhido no vetor
    private boolean trocando;       // já escolheu: as janelas estão trocando
    private double tempo;           // relógio do brilho das estrelas
    private double tempoNaTela;
    private int xMouseAntes;
    private int yMouseAntes;
    private boolean cursorDeMao;

    public MenuInicial() {
        super( LARGURA, ALTURA, "Tetris e Pega Pelúcia", 60, true );
    }

    @Override
    public void create() {

        setExitKey( TrocaDeJanela.NENHUMA_TECLA );   // o ESC é tratado no update

        double xPrimeiro = ( LARGURA - Cartao.LARGURA * 2 - ENTRE_CARTOES ) / 2;
        double xSegundo = xPrimeiro + Cartao.LARGURA + ENTRE_CARTOES;

        cartoes = new Cartao[2];
        cartoes[0] = new Cartao( "Tetris - Fila",
                new String[] { "Fila: a primeira peça que entra", "é a primeira que sai." },
                new PreviaFila( Cartao.LARGURA_PREVIA, Cartao.ALTURA_PREVIA ),
                TrocaDeJanela.TETRIS, xPrimeiro, CARTAO_Y );
        cartoes[1] = new Cartao( "Pega Pelúcia - Pilha",
                new String[] { "Pilha: a última pelúcia que entra", "é a primeira que sai." },
                new PreviaPilha( this, Cartao.LARGURA_PREVIA, Cartao.ALTURA_PREVIA ),
                TrocaDeJanela.PEGA_PELUCIA, xSegundo, CARTAO_Y );

        Random sorteio = new Random();
        estrelas = new Estrela[QUANTIDADE_ESTRELAS];
        for ( int i = 0; i < estrelas.length; i++ ) {
            estrelas[i] = new Estrela( sorteio.nextInt( LARGURA ), sorteio.nextInt( ALTURA ), sorteio.nextDouble() * Math.PI * 2 );
        }

        escolhido = 0;
        xMouseAntes = -1;
        yMouseAntes = -1;

    }

    @Override
    public void update( double delta ) {

        tempo += delta;
        tempoNaTela += delta;

        for ( Cartao cartao : cartoes ) {
            cartao.atualizar( delta );
        }

        if ( trocando ) {
            return;
        }

        if ( isKeyPressed( KEY_LEFT ) || isKeyPressed( KEY_A ) || isKeyPressed( KEY_UP ) || isKeyPressed( KEY_W ) ) {
            escolhido = 0;
        }
        if ( isKeyPressed( KEY_RIGHT ) || isKeyPressed( KEY_D ) || isKeyPressed( KEY_DOWN ) || isKeyPressed( KEY_S ) ) {
            escolhido = 1;
        }
        if ( isKeyPressed( KEY_ENTER ) || isKeyPressed( KEY_SPACE ) ) {
            abrirEscolhido();
            return;
        }

        boolean apertouEsc = isKeyPressed( KEY_ESCAPE );   // lido sempre, mesmo quando vai ser ignorado
        if ( apertouEsc && tempoNaTela > TEMPO_PROTECAO_ESC ) {
            System.exit( 0 );   // o mesmo que o ESC normal da JSGE faz
        }

        atualizarMouse();

    }

    // passar o mouse por cima de um cartão escolhe, clicar abre
    private void atualizarMouse() {

        int xMouse = getMouseX();
        int yMouse = getMouseY();
        boolean mexeu = xMouse != xMouseAntes || yMouse != yMouseAntes;
        xMouseAntes = xMouse;
        yMouseAntes = yMouse;

        int sobOMouse = -1;   // -1 = nenhum cartão
        for ( int i = 0; i < cartoes.length; i++ ) {
            if ( cartoes[i].contem( xMouse, yMouse ) ) {
                sobOMouse = i;
            }
        }

        if ( sobOMouse >= 0 && mexeu ) {
            escolhido = sobOMouse;
        }

        // mãozinha no cursor só em cima de um cartão
        if ( sobOMouse >= 0 && !cursorDeMao ) {
            setMouseCursor( MOUSE_CURSOR_POINTING_HAND );
            cursorDeMao = true;
        } else if ( sobOMouse < 0 && cursorDeMao ) {
            setMouseCursor( MOUSE_CURSOR_DEFAULT );
            cursorDeMao = false;
        }

        if ( sobOMouse >= 0 && isMouseButtonPressed( MOUSE_BUTTON_LEFT ) ) {
            escolhido = sobOMouse;
            abrirEscolhido();
        }

    }

    // fecha o menu e abre a janela do jogo do cartão escolhido
    private void abrirEscolhido() {
        trocando = true;
        TrocaDeJanela troca = new TrocaDeJanela( this, cartoes[escolhido].getDestino() );
        troca.start();
    }

    @Override
    public void draw() {

        clearBackground( FUNDO );

        for ( Estrela estrela : estrelas ) {
            estrela.desenhar( this, tempo );
        }

        setFontSize( 30 );
        String titulo = "ESCOLHA O JOGO";
        drawText( titulo, ( LARGURA - measureText( titulo ) ) / 2.0, 30, GOLD );

        for ( int i = 0; i < cartoes.length; i++ ) {
            boolean estaEscolhido = ( i == escolhido );
            cartoes[i].desenhar( this, estaEscolhido );
        }

        setFontSize( 14 );
        String ajuda = "Setas ou mouse: escolher    Enter ou clique: jogar    Esc: sair";
        drawText( ajuda, ( LARGURA - measureText( ajuda ) ) / 2.0, 394, COR_TEXTO_FRACO );

        setFontSize( 12 );
        String dica = "Dentro do jogo, Esc ou fechar a janela volta para este menu";
        drawText( dica, ( LARGURA - measureText( dica ) ) / 2.0, 418, COR_DICA );

    }

    /** Estrelinha do fundo, que pisca devagar. */
    private static class Estrela {

        private final double x;
        private final double y;
        private final double fase;   // cada uma pisca num momento diferente

        Estrela( double x, double y, double fase ) {
            this.x = x;
            this.y = y;
            this.fase = fase;
        }

        void desenhar( EngineFrame e, double tempo ) {
            int alfa = (int) ( 90 + 80 * Math.sin( tempo * 2 + fase ) );
            e.fillRectangle( x, y, 3, 3, new Color( 255, 255, 255, alfa ) );
        }

    }

    public static void main( String[] args ) {
        new MenuInicial();
    }

}
