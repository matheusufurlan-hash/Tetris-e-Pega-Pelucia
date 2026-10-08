package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import java.awt.event.WindowEvent;

/**
 * Troca a janela aberta: fecha a atual, espera um instante e abre a próxima
 * (o menu ou um dos jogos).
 *
 * Cada janela da JSGE tem o seu próprio laço de jogo e liga/desliga o suporte
 * a controle (gamepad) quando abre e fecha. Esperando a anterior parar de vez,
 * nunca ficam duas rodando ao mesmo tempo. A troca roda numa thread separada
 * para que essa espera não trave nenhuma janela.
 *
 * @author Yoná Maia & Matheus Ubinha
 */
public class TrocaDeJanela extends Thread {

    public static final int MENU = 0;
    public static final int TETRIS = 1;
    public static final int PEGA_PELUCIA = 2;

    public static final int NENHUMA_TECLA = -1;   // nenhuma tecla tem esse código: desliga o ESC da JSGE

    private static final long ESPERA_MILISSEGUNDOS = 200;

    private final EngineFrame janelaAtual;   // janela que vai fechar (null se ela já fechou)
    private final int destino;               // MENU, TETRIS ou PEGA_PELUCIA

    public TrocaDeJanela( EngineFrame janelaAtual, int destino ) {
        this.janelaAtual = janelaAtual;
        this.destino = destino;
    }

    @Override
    public void run() {

        if ( janelaAtual != null ) {
            fecharSemSair( janelaAtual );
        }

        try {
            Thread.sleep( ESPERA_MILISSEGUNDOS );
        } catch ( InterruptedException ex ) {
            // ninguém interrompe esta thread; se acontecer, a próxima janela só abre mais cedo
        }

        if ( destino == MENU ) {
            new MenuInicial();
        } else if ( destino == TETRIS ) {
            prepararJogo( new Main() );
        } else if ( destino == PEGA_PELUCIA ) {
            prepararJogo( new Tela() );
        }

    }

    /**
     * O jogo abre na janela dele, sem mudar nada no código do jogo.
     * Só muda o que acontece ao fechar: em vez de encerrar o programa, o menu volta.
     */
    private void prepararJogo( EngineFrame jogo ) {

        jogo.setDefaultCloseOperation( EngineFrame.DISPOSE_ON_CLOSE );   // o X fecha só o jogo
        jogo.setExitKey( NENHUMA_TECLA );                                // o ESC passa a ser tratado pelo VoltaAoMenu

        VoltaAoMenu voltaAoMenu = new VoltaAoMenu( jogo );
        jogo.getContentPane().getComponent( 0 ).addKeyListener( voltaAoMenu );   // painel onde a JSGE lê o teclado
        jogo.addWindowListener( voltaAoMenu );

    }

    /**
     * Fecha a janela pelo mesmo caminho do botão X, mas sem encerrar o programa:
     * a JSGE para o laço do jogo e a janela é liberada.
     */
    public static void fecharSemSair( EngineFrame janela ) {
        janela.setDefaultCloseOperation( EngineFrame.DISPOSE_ON_CLOSE );
        janela.dispatchEvent( new WindowEvent( janela, WindowEvent.WINDOW_CLOSING ) );
    }

}
