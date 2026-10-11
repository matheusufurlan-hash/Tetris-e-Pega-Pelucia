package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.image.Image;
import br.com.davidbuzatto.jsge.math.MathUtils;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.util.Random;

public class Garra {
    public enum Estado { PARADA, DESCENDO, FECHANDO, SUBINDO }

    private static final double VELOCIDADE_X = 280;
    private static final double VELOCIDADE_Y = 320;
    private static final double TEMPO_FECHAR = 0.4;

    private static final String PASTA_IMAGENS = "src/3x/garra/";
    private static final int QUADRO_ABERTA = 0;
    private static final int QUADRO_SEGURANDO = 3;
    private static final int QUADRO_FECHADA = 4;
    private static final double TEMPO_QUADRO_ABRINDO = 0.06;
    private static final double TEMPO_ENCAIXE = 0.15;

    public static final double DISTANCIA_PELUCIA = 78;
    private static final double AJUSTE_CARRINHO = -12;
    private static final double AJUSTE_CABO = 27;

    private final double forca;
    private final Random random;

    private final Vector2 posicao;
    private final double yTrilho;
    private final double yRepouso;
    private final double yFundo;
    private final double xMin;
    private final double xMax;

    private Estado estado = Estado.PARADA;
    private double yAlvo;
    private double tempoFechando;
    private boolean momentoDeAgarrar;
    private Pelucia carregando;

    private Image[] quadros;
    private Image carrinho;
    private Image cabo;
    private boolean tentouCarregar;
    private int quadroAbrindo = QUADRO_ABERTA;
    private double tempoAbrindo;
    private double encaixeX;
    private double encaixeY;
    private double tempoEncaixe;

    public Garra( double forca, Random random, double xInicial, double xMin, double xMax, double yTrilho, double yRepouso, double yFundo ) {
        this.forca = forca;
        this.random = random;
        this.xMin = xMin;
        this.xMax = xMax;
        this.yTrilho = yTrilho;
        this.yRepouso = yRepouso;
        this.yFundo = yFundo;
        this.yAlvo = yFundo;
        this.posicao = new Vector2( xInicial, yRepouso );
    }

    public double chanceDePegar( Pelucia pelucia ) {
        double chance = forca - pelucia.getTipo().getPeso();
        return MathUtils.clamp( chance, 0.05, 0.95 );
    }

    public boolean tentarPegar( Pelucia pelucia ) {
        return random.nextDouble() < chanceDePegar( pelucia );
    }

    public void mover( int direcao, double delta ) {
        if ( estado == Estado.PARADA ) {
            posicao.x = MathUtils.clamp( posicao.x + direcao * VELOCIDADE_X * delta, xMin, xMax );
        }
    }

    public void alinharX( double x ) {
        if ( estado == Estado.PARADA ) {
            posicao.x = MathUtils.clamp( x, xMin, xMax );
        }
    }

    public boolean iniciarDescida() {
        return iniciarDescida( yFundo );

    }

    public boolean iniciarDescida( double yDestino ) {
        if ( estado != Estado.PARADA ) {
            return false;
        }

        yAlvo = MathUtils.clamp( yDestino, yRepouso, yFundo );
        estado = Estado.DESCENDO;
        return true;
    }

    public void atualizar( double delta ) {
        switch ( estado ) {
            case DESCENDO -> {
                posicao.y += VELOCIDADE_Y * delta;
                if ( posicao.y >= yAlvo ) {
                    posicao.y = yAlvo;
                    estado = Estado.FECHANDO;
                    tempoFechando = 0;
                }
            }
            case FECHANDO -> {
                tempoFechando += delta;
                if ( tempoFechando >= TEMPO_FECHAR ) {
                    momentoDeAgarrar = true;
                    estado = Estado.SUBINDO;
                }
            }
            case SUBINDO -> {
                posicao.y -= VELOCIDADE_Y * delta;
                if ( posicao.y <= yRepouso ) {
                    posicao.y = yRepouso;
                    estado = Estado.PARADA;
                    if ( carregando == null ) {
                        comecarAbrir( QUADRO_FECHADA );
                    }
                }
            }
            default -> { }
        }

        if ( quadroAbrindo > QUADRO_ABERTA ) {
            tempoAbrindo += delta;
            if ( tempoAbrindo >= TEMPO_QUADRO_ABRINDO ) {
                tempoAbrindo = 0;
                quadroAbrindo--;
            }
        }

        if ( carregando != null ) {
            tempoEncaixe -= delta;
            if ( tempoEncaixe < 0 ) {
                tempoEncaixe = 0;
            }
            acompanhar();
        }

    }

    public boolean momentoAgarrar() {
        boolean r = momentoDeAgarrar;
        momentoDeAgarrar = false;
        return r;
    }

    public void carregar( Pelucia pelucia ) {
        this.carregando = pelucia;
        encaixeX = pelucia.getPosicao().x - posicao.x;
        encaixeY = pelucia.getPosicao().y - ( posicao.y + DISTANCIA_PELUCIA );
        tempoEncaixe = TEMPO_ENCAIXE;
        acompanhar();
    }

    public Pelucia soltar() {
        Pelucia p = carregando;
        carregando = null;
        if ( p != null ) {
            comecarAbrir( QUADRO_SEGURANDO );
        }
        return p;
    }

    private void acompanhar() {
        double falta = tempoEncaixe / TEMPO_ENCAIXE;
        double x = posicao.x + encaixeX * falta;
        double y = posicao.y + DISTANCIA_PELUCIA + encaixeY * falta;
        carregando.moverPara( x, y );
    }

    private void comecarAbrir( int quadroInicial ) {
        quadroAbrindo = quadroInicial;
        tempoAbrindo = 0;
    }

    private int quadroAtual() {
        if ( carregando != null ) {
            return QUADRO_SEGURANDO;
        }

        if ( estado == Estado.FECHANDO ) {
            int quadro = (int) ( tempoFechando / ( TEMPO_FECHAR / 3 ) );
            if ( quadro > 2 ) {
                quadro = 2;
            }
            return quadro;
        }

        if ( estado == Estado.SUBINDO ) {
            return QUADRO_FECHADA;
        }

        return quadroAbrindo;

    }

    private void carregarImagens( EngineFrame e ) {
        if ( tentouCarregar ) {
            return;
        }
        tentouCarregar = true;

        quadros = new Image[5];
        for ( int i = 0; i < quadros.length; i++ ) {
            quadros[i] = e.loadImage( PASTA_IMAGENS + "garra_" + i + ".png" );
        }
        carrinho = e.loadImage( PASTA_IMAGENS + "garra_carrinho.png" );
        cabo = e.loadImage( PASTA_IMAGENS + "garra_cabo.png" );

    }

    public void desenhar( EngineFrame e ) {
        carregarImagens( e );

        double x = posicao.x;
        double y = posicao.y;

        e.drawImage( carrinho, x - carrinho.getWidth() / 2.0, yTrilho + AJUSTE_CARRINHO );

        double yCabo = yTrilho + AJUSTE_CABO;
        while ( yCabo < y ) {
            e.drawImage( cabo, x - cabo.getWidth() / 2.0, yCabo );
            yCabo += cabo.getHeight();
        }

        if ( carregando != null ) {
            Vector2 posicaoPelucia = carregando.getPosicao();
            carregando.getTipo().desenhar( e, posicaoPelucia.x, posicaoPelucia.y, Pelucia.Raio );
        }

        Image imagem = quadros[quadroAtual()];
        e.drawImage( imagem, x - imagem.getWidth() / 2.0, y );

    }

    public double getX() { return posicao.x; }
    public Estado getEstado() { return estado; }
    public Pelucia getCarregando() { return carregando; }
    public double getForca() { return forca; }

}
