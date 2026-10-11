package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class VoltaAoMenu extends WindowAdapter implements KeyListener {
    private final EngineFrame jogo;

    public VoltaAoMenu( EngineFrame jogo ) {
        this.jogo = jogo;
    }

    @Override
    public void keyPressed( KeyEvent e ) {
        if ( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
            TrocaDeJanela.fecharSemSair( jogo );
        }
    }

    @Override
    public void keyReleased( KeyEvent e ) {
    }

    @Override
    public void keyTyped( KeyEvent e ) {
    }

    @Override
    public void windowClosed( WindowEvent e ) {
        TrocaDeJanela troca = new TrocaDeJanela( null, TrocaDeJanela.MENU );
        troca.start();
    }

}
