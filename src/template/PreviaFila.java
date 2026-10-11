package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import java.awt.Color;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class PreviaFila extends Previa {
    private static final int PECAS_A_VISTA = 4;
    private static final double TEMPO_ENTRE_PASSOS = 1.2;
    private static final double VELOCIDADE_DESLIZE = 3;
    private static final double TAMANHO_BLOCO = 14;

    private static final Color COR_FUNDO = new Color( 235, 235, 240 );
    private static final Color COR_ESTEIRA = new Color( 210, 210, 218 );
    private static final Color COR_LEGENDA = new Color( 120, 120, 130 );

    private final Random sorteio;
    private final Queue<TipoPeca> fila;
    private TipoPeca pecaQueSaiu;
    private double tempoParada;
    private double deslize;

    public PreviaFila( double largura, double altura ) {
        super( largura, altura );
        sorteio = new Random();
        fila = new LinkedList<>();

        for ( int i = 0; i < PECAS_A_VISTA; i++ ) {
            fila.offer( TipoPeca.sortear( sorteio ) );
        }

    }

    @Override
    public void atualizar( double delta ) {
        if ( deslize > 0 ) {
            deslize -= VELOCIDADE_DESLIZE * delta;
            if ( deslize < 0 ) {
                deslize = 0;
            }
        }

        tempoParada += delta;
        if ( tempoParada >= TEMPO_ENTRE_PASSOS ) {
            tempoParada = 0;
            pecaQueSaiu = fila.poll();
            fila.offer( TipoPeca.sortear( sorteio ) );
            deslize = 1;
        }

    }

    @Override
    public void desenhar( EngineFrame e, double x, double y ) {
        e.fillRectangle( x, y, largura, altura, COR_FUNDO );

        double vaga = largura / PECAS_A_VISTA;
        double yCentro = y + altura / 2 - 10;
        e.fillRoundRectangle( x + 6, yCentro - 34, largura - 12, 68, 0.3, COR_ESTEIRA );

        if ( pecaQueSaiu != null && deslize > 0 ) {
            desenharPeca( e, pecaQueSaiu, x + vaga * ( deslize - 0.5 ), yCentro );
        }

        int posicao = 0;
        for ( TipoPeca tipo : fila ) {
            desenharPeca( e, tipo, x + vaga * ( posicao + 0.5 + deslize ), yCentro );
            posicao++;
        }

        desenharLegenda( e, x, y + altura - 30 );

    }

    private void desenharLegenda( EngineFrame e, double x, double y ) {
        e.setFontSize( 12 );

        e.fillTriangle( x + 12, y + 7, x + 22, y + 1, x + 22, y + 13, COR_LEGENDA );
        e.drawText( "sai", x + 26, y, COR_LEGENDA );

        double direita = x + largura;
        e.fillTriangle( direita - 22, y + 7, direita - 12, y + 1, direita - 12, y + 13, COR_LEGENDA );
        e.drawText( "entra", direita - 26 - e.measureText( "entra" ), y, COR_LEGENDA );

    }

    private void desenharPeca( EngineFrame e, TipoPeca tipo, double cx, double cy ) {
        int[][] formato = tipo.getFormato();

        int menorLinha = formato[0][0];
        int maiorLinha = formato[0][0];
        int menorColuna = formato[0][1];
        int maiorColuna = formato[0][1];
        for ( int[] bloco : formato ) {
            menorLinha = Math.min( menorLinha, bloco[0] );
            maiorLinha = Math.max( maiorLinha, bloco[0] );
            menorColuna = Math.min( menorColuna, bloco[1] );
            maiorColuna = Math.max( maiorColuna, bloco[1] );
        }

        double larguraPeca = ( maiorColuna - menorColuna + 1 ) * TAMANHO_BLOCO;
        double alturaPeca = ( maiorLinha - menorLinha + 1 ) * TAMANHO_BLOCO;
        double x0 = cx - larguraPeca / 2;
        double y0 = cy - alturaPeca / 2;

        for ( int[] bloco : formato ) {
            double xBloco = x0 + ( bloco[1] - menorColuna ) * TAMANHO_BLOCO;
            double yBloco = y0 + ( bloco[0] - menorLinha ) * TAMANHO_BLOCO;
            e.fillRectangle( xBloco, yBloco, TAMANHO_BLOCO, TAMANHO_BLOCO, tipo.getCor() );
            e.drawRectangle( xBloco, yBloco, TAMANHO_BLOCO, TAMANHO_BLOCO, tipo.getCor().darker() );
        }

    }

}
