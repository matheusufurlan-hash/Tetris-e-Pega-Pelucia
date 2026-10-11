package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.image.Image;
import java.awt.Color;
import java.util.List;

public class PreviaPilha extends Previa {
    private enum Etapa { ESPERANDO, DESCENDO, FECHANDO, SUBINDO, ABRINDO }

    private static final double RAIO_PELUCIA = 32;
    private static final double ALTURA_ANDAR = 36;
    private static final double LARGURA_GARRA = 96;
    private static final double ALTURA_GARRA = 72;
    private static final double DISTANCIA_PELUCIA = 52;
    private static final double TAMANHO_CABO = 8;

    private static final double VELOCIDADE = 90;
    private static final double TEMPO_FECHAR = 0.3;
    private static final double TEMPO_QUADRO_ABRINDO = 0.08;
    private static final double TEMPO_ESPERA = 0.5;

    private static final Color COR_VIDRO = new Color( 196, 228, 252 );
    private static final Color COR_CHAO = new Color( 184, 160, 230 );
    private static final Color COR_TRILHO = new Color( 150, 160, 180 );

    private final Image[] quadros;
    private final Image cabo;
    private final Pilha<TipoPelucia> pilha;
    private TipoPelucia naGarra;

    private final double yGarraEmCima;
    private final double yGarraNaPilha;
    private final double yBase;

    private Etapa etapa;
    private double yGarra;
    private double tempoEtapa;
    private int quadro;

    public PreviaPilha( EngineFrame e, double largura, double altura ) {
        super( largura, altura );

        quadros = new Image[5];
        for ( int i = 0; i < quadros.length; i++ ) {
            quadros[i] = e.loadImage( "src/3x/garra/garra_" + i + ".png" );
        }
        cabo = e.loadImage( "src/3x/garra/garra_cabo.png" );

        pilha = new Pilha<>();
        pilha.empilhar( TipoPelucia.URSO );
        pilha.empilhar( TipoPelucia.COELHO );

        yBase = altura - 6 - RAIO_PELUCIA;
        double yTopo = yBase - ALTURA_ANDAR;
        yGarraNaPilha = yTopo - DISTANCIA_PELUCIA;
        yGarraEmCima = 6;

        yGarra = yGarraEmCima;
        etapa = Etapa.ESPERANDO;
        quadro = 0;

    }

    @Override
    public void atualizar( double delta ) {
        switch ( etapa ) {
            case ESPERANDO:
                tempoEtapa += delta;
                if ( tempoEtapa >= TEMPO_ESPERA ) {
                    etapa = Etapa.DESCENDO;
                }
                break;

            case DESCENDO:
                yGarra += VELOCIDADE * delta;
                if ( yGarra >= yGarraNaPilha ) {
                    yGarra = yGarraNaPilha;
                    tempoEtapa = 0;
                    if ( naGarra == null ) {
                        etapa = Etapa.FECHANDO;
                    } else {
                        pilha.empilhar( naGarra );
                        naGarra = null;
                        etapa = Etapa.ABRINDO;
                    }
                }
                break;

            case FECHANDO:
                tempoEtapa += delta;
                quadro = (int) ( tempoEtapa / ( TEMPO_FECHAR / 3 ) );
                if ( quadro > 2 ) {
                    quadro = 2;
                }
                if ( tempoEtapa >= TEMPO_FECHAR ) {
                    naGarra = pilha.desempilhar();
                    quadro = 3;
                    etapa = Etapa.SUBINDO;
                }
                break;

            case SUBINDO:
                yGarra -= VELOCIDADE * delta;
                if ( yGarra <= yGarraEmCima ) {
                    yGarra = yGarraEmCima;
                    tempoEtapa = 0;
                    etapa = Etapa.ESPERANDO;
                }
                break;

            case ABRINDO:
                tempoEtapa += delta;
                if ( tempoEtapa >= TEMPO_QUADRO_ABRINDO ) {
                    tempoEtapa = 0;
                    quadro--;
                    if ( quadro == 0 ) {
                        etapa = Etapa.SUBINDO;
                    }
                }
                break;

        }

    }

    @Override
    public void desenhar( EngineFrame e, double x, double y ) {
        e.fillRectangle( x, y, largura, altura, COR_VIDRO );
        e.fillRectangle( x, y + altura - 10, largura, 10, COR_CHAO );
        e.fillRectangle( x, y, largura, 5, COR_TRILHO );

        double cx = x + largura / 2;
        double yDaGarra = y + yGarra;

        double yCabo = y + 5;
        while ( yCabo < yDaGarra ) {
            e.drawImage( cabo, 0, 0, cabo.getWidth(), cabo.getHeight(), cx - TAMANHO_CABO / 2, yCabo, TAMANHO_CABO, TAMANHO_CABO );
            yCabo += TAMANHO_CABO;
        }

        if ( naGarra != null ) {
            naGarra.desenhar( e, cx, yDaGarra + DISTANCIA_PELUCIA, RAIO_PELUCIA );
        }

        List<TipoPelucia> itens = pilha.paraLista();
        for ( int i = itens.size() - 1; i >= 0; i-- ) {
            itens.get( i ).desenhar( e, cx, y + yBase - i * ALTURA_ANDAR, RAIO_PELUCIA );
        }

        Image imagem = quadros[quadro];
        e.drawImage( imagem, 0, 0, imagem.getWidth(), imagem.getHeight(), cx - LARGURA_GARRA / 2, yDaGarra, LARGURA_GARRA, ALTURA_GARRA );

    }

}
