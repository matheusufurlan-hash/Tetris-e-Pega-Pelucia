package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.image.Image;

/**
 * @author Yoná Maia & Matheus Ubinha
 */

public enum TipoPelucia {

    // imagens de 96 x 96 (arte 32 x 32 ampliada 3x)
    SAPO( "Sapo", 0.15, "src/3x/pelucias/sapo.png" ),
    LEAO( "Leão", 0.30, "src/3x/pelucias/leao.png" ),
    COELHO( "Coelho", 0.20, "src/3x/pelucias/coelho.png" ),
    CROCODILO( "Crocodilo", 0.35, "src/3x/pelucias/jacare.png" ),
    URSO( "Urso", 0.40, "src/3x/pelucias/urso.png" ),
    PATO( "Pato", 0.10, "src/3x/pelucias/patinho.png" );

    private final String nome;
    private final double peso;
    private final String caminhoImagem;

    private Image imagem;
    private boolean tentouCarregar;

    TipoPelucia( String nome, double peso, String caminhoImagem ) {
        this.nome = nome;
        this.peso = peso;
        this.caminhoImagem = caminhoImagem;
    }

    public String getNome() { return nome; }
    public double getPeso() { return peso; }

    private void carregarImagem( EngineFrame e ) {

        if ( tentouCarregar ) {
            return;
        }
        tentouCarregar = true;

        try {
            imagem = e.loadImage( caminhoImagem );
            
        } catch ( Exception ex ) {
            imagem = null;
            System.err.println( "Imagem não carregada: " + caminhoImagem );
            
        }

    }

    // desenha a pelúcia centralizada em (cx, cy), num quadrado de lado 2r.
    // r = Pelucia.Raio usa a imagem no tamanho original; um r menor encolhe (ícones do letreiro)
    public void desenhar( EngineFrame e, double cx, double cy, double r ) {

        carregarImagem( e );

        if ( imagem != null ) {
            e.drawImage( imagem, 0, 0, imagem.getWidth(), imagem.getHeight(), cx - r, cy - r, r * 2, r * 2 );
        }

    }

}