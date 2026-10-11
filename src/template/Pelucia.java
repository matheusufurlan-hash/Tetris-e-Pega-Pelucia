package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.awt.Color;

public class Pelucia {
    public static final double Raio = 48;
    private static final double GRAVIDADE = 1500;

    private final TipoPelucia tipo;
    private final Vector2 posicao;
    private final Vector2 velocidade;

    private boolean caindo;
    private boolean pousou;
    private double chaoY;

    public Pelucia( TipoPelucia tipo ) {
        this( tipo, 0, 0 );
    }

    public Pelucia( TipoPelucia tipo, double x, double y ) {
        this.tipo = tipo;
        this.posicao = new Vector2( x, y );
        this.velocidade = new Vector2();
    }

    public void soltar( double chaoY ) {
        this.chaoY = chaoY;
        this.velocidade.y = 0;
        this.caindo = true;
        this.pousou = false;
    }

    public void atualizar( double delta ) {
        if ( caindo ) {
            velocidade.y += GRAVIDADE * delta;
            posicao.y += velocidade.y * delta;
            if ( posicao.y >= chaoY ) {
                posicao.y = chaoY;
                velocidade.y = 0;
                caindo = false;
                pousou = true;
            }
        }
    }

    public boolean consumirPouso() {
        boolean r = pousou;
        pousou = false;
        return r;
    }

    public void moverPara( double x, double y ) {
        posicao.x = x;
        posicao.y = y;
    }

    public void desenhar( EngineFrame e ) {
        if ( !caindo ) {
            e.fillEllipse( posicao.x, posicao.y + Raio * 0.95, Raio * 0.9, Raio * 0.18, new Color( 0, 0, 0, 70 ) );
        }
        tipo.desenhar( e, posicao.x, posicao.y, Raio );
    }

    public TipoPelucia getTipo() { return tipo; }
    public Vector2 getPosicao() { return posicao; }
    public boolean isCaindo() { return caindo; }

    @Override
    public String toString() { return tipo.getNome(); }

}
