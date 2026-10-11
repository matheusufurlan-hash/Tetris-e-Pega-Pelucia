package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.MathUtils;

public class Cronometro {
    private double duracao = 1;
    private double restante;

    public void iniciar( double segundos ) {
        duracao = segundos;
        restante = segundos;
    }

    public void atualizar( double delta ) {
        restante = MathUtils.clamp( restante - delta, 0, duracao );
    }

    public void acrescentar( double segundos ) {
        restante += segundos;
        if ( restante > duracao ) {
            duracao = restante;
        }
    }

    public double getRestante() { return restante; }

    public boolean terminou() { return restante <= 0; }

    public void desenhar( EngineFrame e, double x, double y, double largura, double altura ) {
        double fracao = restante / duracao;

        e.fillRoundRectangle( x, y, largura, altura, 0.4, new java.awt.Color( 25, 22, 50 ) );

        if ( largura * fracao > 4 ) {
            var cor = fracao > 0.5 ? EngineFrame.GREEN : fracao > 0.2 ? EngineFrame.ORANGE : EngineFrame.RED;

            e.fillRoundRectangle( x, y, largura * fracao, altura, 0.4, cor );
        }

        e.setFontSize( 14 );
        String t = String.format( "%.1fs", restante );

        e.drawText( t, x + ( largura - e.measureText( t ) ) / 2.0, y + 1, EngineFrame.WHITE );

    }

}
