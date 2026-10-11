package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;

public abstract class Previa {
    protected final double largura;
    protected final double altura;

    public Previa( double largura, double altura ) {
        this.largura = largura;
        this.altura = altura;
    }

    public abstract void atualizar( double delta );

    public abstract void desenhar( EngineFrame e, double x, double y );

}
