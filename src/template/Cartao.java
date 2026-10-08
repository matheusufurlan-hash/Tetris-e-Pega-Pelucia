package template;

import br.com.davidbuzatto.jsge.collision.CollisionUtils;
import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.geom.Rectangle;
import java.awt.Color;

/**
 * Um cartão do menu: a prévia animada, o nome do jogo e uma explicação da
 * estrutura de dados que ele usa.
 *
 * @author Yoná Maia & Matheus Ubinha
 */
public class Cartao {

    public static final double LARGURA = 320;
    public static final double ALTURA = 280;
    private static final double MARGEM = 14;
    public static final double LARGURA_PREVIA = LARGURA - MARGEM * 2;
    public static final double ALTURA_PREVIA = 168;

    private static final double SUBIDA = 6;   // o cartão escolhido sobe um pouco

    private static final Color COR_FUNDO = new Color( 52, 38, 96 );
    private static final Color COR_FUNDO_ESCOLHIDO = new Color( 70, 50, 128 );
    private static final Color COR_BORDA = new Color( 96, 84, 150 );
    private static final Color COR_TEXTO_FRACO = new Color( 190, 180, 230 );
    private static final Color COR_APAGADO = new Color( 29, 19, 54, 110 );   // véu sobre a prévia do cartão não escolhido

    private final String nome;
    private final String[] explicacao;
    private final Previa previa;
    private final int destino;          // janela que este cartão abre (TrocaDeJanela.TETRIS ou PEGA_PELUCIA)
    private final Rectangle area;
    private double tempo;               // para o brilho pulsar

    public Cartao( String nome, String[] explicacao, Previa previa, int destino, double x, double y ) {
        this.nome = nome;
        this.explicacao = explicacao;
        this.previa = previa;
        this.destino = destino;
        this.area = new Rectangle( x, y, LARGURA, ALTURA );
    }

    public void atualizar( double delta ) {
        tempo += delta;
        previa.atualizar( delta );
    }

    public boolean contem( double px, double py ) {
        return CollisionUtils.checkCollisionPointRectangle( px, py, area );
    }

    public void desenhar( EngineFrame e, boolean escolhido ) {

        double x = area.x;
        double y = area.y;
        Color corFundo = COR_FUNDO;
        Color corBorda = COR_BORDA;
        Color corNome = COR_TEXTO_FRACO;
        float espessuraBorda = 2f;

        if ( escolhido ) {
            y = y - SUBIDA;
            corFundo = COR_FUNDO_ESCOLHIDO;
            corBorda = EngineFrame.GOLD;
            corNome = EngineFrame.WHITE;
            espessuraBorda = 4f;
            desenharBrilho( e, x, y );
        }

        e.fillRoundRectangle( x, y, LARGURA, ALTURA, 0.08, corFundo );
        e.setStrokeLineWidth( espessuraBorda );
        e.drawRoundRectangle( x, y, LARGURA, ALTURA, 0.08, corBorda );
        e.setStrokeLineWidth( 1f );

        // prévia (o recorte não deixa nada da animação passar da borda dela)
        double xPrevia = x + MARGEM;
        double yPrevia = y + MARGEM;
        e.beginScissorMode( xPrevia, yPrevia, LARGURA_PREVIA, ALTURA_PREVIA );
        previa.desenhar( e, xPrevia, yPrevia );
        if ( !escolhido ) {
            e.fillRectangle( xPrevia, yPrevia, LARGURA_PREVIA, ALTURA_PREVIA, COR_APAGADO );
        }
        e.endScissorMode();

        // nome do jogo e explicação, centralizados
        double yTexto = yPrevia + ALTURA_PREVIA;
        e.setFontSize( 22 );
        e.drawText( nome, x + ( LARGURA - e.measureText( nome ) ) / 2.0, yTexto + 16, corNome );

        e.setFontSize( 13 );
        for ( int i = 0; i < explicacao.length; i++ ) {
            String linha = explicacao[i];
            e.drawText( linha, x + ( LARGURA - e.measureText( linha ) ) / 2.0, yTexto + 52 + i * 18, COR_TEXTO_FRACO );
        }

    }

    // contorno dourado extra, pulsando, em volta do cartão escolhido
    private void desenharBrilho( EngineFrame e, double x, double y ) {
        int alfa = (int) ( 130 + 70 * Math.sin( tempo * 4 ) );
        e.setStrokeLineWidth( 2f );
        e.drawRoundRectangle( x - 7, y - 7, LARGURA + 14, ALTURA + 14, 0.08, new Color( 255, 203, 0, alfa ) );
    }

    public int getDestino() { return destino; }

}
