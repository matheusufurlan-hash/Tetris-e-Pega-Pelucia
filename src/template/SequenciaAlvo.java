package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.MathUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Yoná Maia & Matheus Ubinha
 */

public class SequenciaAlvo {

    public static final double ESPACO = 64;
    public static final double ICONE_RAIO = 32;   // ícones de 64 px: cada pixel da arte vira 2, sem borrar

    public static final int MAX_POR_TIPO = 2;

    private final List<TipoPelucia> tipos;
    private int progresso;

    public SequenciaAlvo( List<TipoPelucia> tipos ) {
        this.tipos = new ArrayList<>( tipos );
    }


    public static SequenciaAlvo gerar( int tamanho ) {
        TipoPelucia[] todos = TipoPelucia.values();
        List<TipoPelucia> lista = new ArrayList<>();
        
        for ( int i = 0; i < tamanho; i++ ) {
            TipoPelucia t;
            
            do {
                t = todos[MathUtils.getRandomValue( 0, todos.length - 1 )];
                
            } while ( Collections.frequency( lista, t ) >= MAX_POR_TIPO );
            
            lista.add( t );
        }
        return new SequenciaAlvo( lista );
    }

    public TipoPelucia getEsperado() {
        return tipos.get( progresso );
        
    }

    public void avancar() { progresso++; }

    public boolean concluida() { return progresso >= tipos.size(); }

    public int getProgresso() { return progresso; }

    public int tamanho() { return tipos.size(); }

    public List<TipoPelucia> getOrdemDeEmpilhar() {
        return Collections.unmodifiableList( tipos );
    }

    public List<TipoPelucia> getOrdemDeDesempilhar() {
        List<TipoPelucia> inversa = new ArrayList<>( tipos );
        Collections.reverse( inversa );
        return inversa;
    }

    //Ordem da spelucias riscadas e nao riscadas
    
    public void desenhar( EngineFrame e, double x, double y, double larguraTotal, boolean ordemInversa, int feitos ) {

        List<TipoPelucia> lista = ordemInversa ? getOrdemDeDesempilhar() : getOrdemDeEmpilhar();

        double espaco = Math.min( ESPACO, larguraTotal / lista.size() );
        double larguraCelula = Math.min( 60, espaco - 2 );
        double meia = larguraCelula / 2;

        for ( int i = 0; i < lista.size(); i++ ) {

            double cx = x + espaco / 2 + i * espaco;

            if ( i == feitos ) {
                e.fillRoundRectangle( cx - meia, y - 32, larguraCelula, 64, 0.3, new Color( 255, 203, 0, 90 ) );
                e.drawRoundRectangle( cx - meia, y - 32, larguraCelula, 64, 0.3, EngineFrame.GOLD );
            }

            lista.get( i ).desenhar( e, cx, y, ICONE_RAIO );

            // itens já feitos com véu escuro e um X vermelho
            if ( i < feitos ) {
                double mx = Math.min( 18, meia - 4 );
                e.fillRoundRectangle( cx - meia, y - 32, larguraCelula, 64, 0.3, new Color( 44, 40, 86, 160 ) );
                e.setStrokeLineWidth( 3f );
                e.drawLine( cx - mx, y - 18, cx + mx, y + 18, EngineFrame.RED );
                e.drawLine( cx - mx, y + 18, cx + mx, y - 18, EngineFrame.RED );
                e.setStrokeLineWidth( 2f );
            }

            e.setFontSize( 11 );
            e.drawText( String.valueOf( i + 1 ), cx - meia + 2, y - 31, EngineFrame.LIGHTGRAY );

        }

    }

}