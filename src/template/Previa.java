package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;

/**
 * Quadro animado que aparece no alto de cada cartão do menu.
 * Cada jogo tem a sua prévia (PreviaFila e PreviaPilha); o cartão só
 * precisa saber atualizar e desenhar uma Previa, sem saber qual é.
 *
 * @author Yoná Maia & Matheus Ubinha
 */
public abstract class Previa {

    protected final double largura;
    protected final double altura;

    public Previa( double largura, double altura ) {
        this.largura = largura;
        this.altura = altura;
    }

    public abstract void atualizar( double delta );

    /** Desenha a prévia com o canto de cima, à esquerda, em (x, y). */
    public abstract void desenhar( EngineFrame e, double x, double y );

}
