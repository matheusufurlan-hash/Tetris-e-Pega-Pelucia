package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import java.awt.Color;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Prévia do Tetris no menu: uma fila de peças andando da direita para a
 * esquerda. A peça da frente sai e uma nova entra no fim da fila.
 *
 * @author Yoná Maia & Matheus Ubinha
 */
public class PreviaFila extends Previa {

    private static final int PECAS_A_VISTA = 4;
    private static final double TEMPO_ENTRE_PASSOS = 1.2;   // a fila anda a cada 1,2 segundo
    private static final double VELOCIDADE_DESLIZE = 3;     // vagas por segundo
    private static final double TAMANHO_BLOCO = 14;

    private static final Color COR_FUNDO = new Color( 235, 235, 240 );   // mesmo fundo do tabuleiro do Tetris
    private static final Color COR_ESTEIRA = new Color( 210, 210, 218 );
    private static final Color COR_LEGENDA = new Color( 120, 120, 130 );

    private final Random sorteio;
    private final Queue<TipoPeca> fila;
    private TipoPeca pecaQueSaiu;   // continua aparecendo enquanto desliza para fora
    private double tempoParada;
    private double deslize;         // quanto as peças ainda vão andar: 1 = uma vaga inteira, 0 = paradas

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

        // as peças deslizam até chegar na vaga delas
        if ( deslize > 0 ) {
            deslize -= VELOCIDADE_DESLIZE * delta;
            if ( deslize < 0 ) {
                deslize = 0;
            }
        }

        tempoParada += delta;
        if ( tempoParada >= TEMPO_ENTRE_PASSOS ) {
            tempoParada = 0;
            pecaQueSaiu = fila.poll();                   // sai da frente da fila
            fila.offer( TipoPeca.sortear( sorteio ) );   // entra no fim da fila
            deslize = 1;                                 // todas andam uma vaga para a frente
        }

    }

    @Override
    public void desenhar( EngineFrame e, double x, double y ) {

        e.fillRectangle( x, y, largura, altura, COR_FUNDO );

        double vaga = largura / PECAS_A_VISTA;
        double yCentro = y + altura / 2 - 10;
        e.fillRoundRectangle( x + 6, yCentro - 34, largura - 12, 68, 0.3, COR_ESTEIRA );

        // a peça que saiu ainda está passando pela borda da esquerda
        if ( pecaQueSaiu != null && deslize > 0 ) {
            desenharPeca( e, pecaQueSaiu, x + vaga * ( deslize - 0.5 ), yCentro );
        }

        // percorre a fila da frente para o fim, sem remover nada
        int posicao = 0;
        for ( TipoPeca tipo : fila ) {
            desenharPeca( e, tipo, x + vaga * ( posicao + 0.5 + deslize ), yCentro );
            posicao++;
        }

        desenharLegenda( e, x, y + altura - 30 );

    }

    // a fila anda da direita (onde a peça entra) para a esquerda (onde ela sai)
    private void desenharLegenda( EngineFrame e, double x, double y ) {

        e.setFontSize( 12 );

        e.fillTriangle( x + 12, y + 7, x + 22, y + 1, x + 22, y + 13, COR_LEGENDA );
        e.drawText( "sai", x + 26, y, COR_LEGENDA );

        double direita = x + largura;
        e.fillTriangle( direita - 22, y + 7, direita - 12, y + 1, direita - 12, y + 13, COR_LEGENDA );
        e.drawText( "entra", direita - 26 - e.measureText( "entra" ), y, COR_LEGENDA );

    }

    // desenha a peça centralizada em (cx, cy), do mesmo jeito que a fila do Tetris
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
