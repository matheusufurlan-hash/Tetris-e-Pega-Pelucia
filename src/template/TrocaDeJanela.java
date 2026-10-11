package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import java.awt.event.WindowEvent;

public class TrocaDeJanela extends Thread {
    public static final int MENU = 0;
    public static final int TETRIS = 1;
    public static final int PEGA_PELUCIA = 2;

    public static final int NENHUMA_TECLA = -1;

    private static final long ESPERA_MILISSEGUNDOS = 200;

    private final EngineFrame janelaAtual;
    private final int destino;

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
        }

        if ( destino == MENU ) {
            new MenuInicial();
        } else if ( destino == TETRIS ) {
            prepararJogo( new Main() );
        } else if ( destino == PEGA_PELUCIA ) {
            prepararJogo( new Tela() );
        }

    }

    private void prepararJogo( EngineFrame jogo ) {
        jogo.setDefaultCloseOperation( EngineFrame.DISPOSE_ON_CLOSE );
        jogo.setExitKey( NENHUMA_TECLA );

        VoltaAoMenu voltaAoMenu = new VoltaAoMenu( jogo );
        jogo.getContentPane().getComponent( 0 ).addKeyListener( voltaAoMenu );
        jogo.addWindowListener( voltaAoMenu );

    }

    public static void fecharSemSair( EngineFrame janela ) {
        janela.setDefaultCloseOperation( EngineFrame.DISPOSE_ON_CLOSE );
        janela.dispatchEvent( new WindowEvent( janela, WindowEvent.WINDOW_CLOSING ) );
    }

}
