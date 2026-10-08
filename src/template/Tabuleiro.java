

package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import java.awt.Color;

public class Tabuleiro {
    public static final int LARGURA_BLOCO = 20;
    public static final int ALTURA_BLOCO = 20;

    private static final Color COR_FUNDO = new Color(235, 235, 240);
    private static final Color COR_GRADE = new Color(210, 210, 218);
    private static final Color COR_BORDA = new Color(70, 70, 80);

    private final int linhas;
    private final int colunas;
    private final int posx;                 // canto superior esquerdo do tabuleiro na tela (pixels)
    private final int posy;
    private final Color[][] gradeDeCores;   // [linha][coluna]; null = celula vazia

    public Tabuleiro(int linhas, int colunas, int posx, int posy) {
        this.linhas = linhas;
        this.colunas = colunas;
        this.posx = posx;
        this.posy = posy;
        this.gradeDeCores = new Color[linhas][colunas];
    }
    //fora da grade == (eh parede)
    //dentro conta como preenchido se tiber cor a celula
    public boolean estaPreenchido(int linha, int coluna) {
        if (linha < 0 || linha >= linhas || coluna < 0 || coluna >= colunas) {
            return true;
        }
        return gradeDeCores[linha][coluna] != null;
    }
    // Grava uma cor numa celula e ignora posicoes fora da grade
    public void ocupar(int linha, int coluna, Color cor) {
        if (linha >= 0 && linha < linhas && coluna >= 0 && coluna < colunas) {
            gradeDeCores[linha][coluna] = cor;
        }
    }
    //Remove todas as linhas completas e faz o que esta em cima cair
    //Devolve quantas linhas foram removidas
    public int limparLinhasCompletas() {
        int removidas = 0;
        int linha = linhas - 1;

        while (linha >= 0) {
            if (linhaCompleta(linha)) {
                removerLinha(linha);
                removidas++;
                // Nao sobe: a linha de cima acabou de descer para ca e precisa ser testada.
            } else {
                linha--;
            }
        }

        return removidas;
    }

    private boolean linhaCompleta(int linha) {
        for (int coluna = 0; coluna < colunas; coluna++) {
            if (gradeDeCores[linha][coluna] == null) {
                return false;
            }
        }
        return true;
    }
    //copia cada linha de cima removida para uma linha abaixo e esvazia o topo
    private void removerLinha(int linhaRemovida) {
        for (int linha = linhaRemovida; linha > 0; linha--) {
            for (int coluna = 0; coluna < colunas; coluna++) {
                gradeDeCores[linha][coluna] = gradeDeCores[linha - 1][coluna];
            }
        }
        for (int coluna = 0; coluna < colunas; coluna++) {
            gradeDeCores[0][coluna] = null;
        }
    }
    // Converte uma coluna de grade para o x em pixels
    private int colunaParaX(int coluna) {
        return posx + coluna * LARGURA_BLOCO;
    }
    //converte uma linha de grade para o y em pixels
    private int linhaParaY(int linha) {
        return posy + linha * ALTURA_BLOCO;
    }
    //desenha um bloco preenchido numa celula. Usado tambem pela peca que esta caindo
    public void desenharBloco(EngineFrame engine, int linha, int coluna, Color cor){
        int x = colunaParaX(coluna);
        int y = linhaParaY(linha);
        engine.fillRectangle(x, y, LARGURA_BLOCO, ALTURA_BLOCO, cor);
        engine.drawRectangle(x, y, LARGURA_BLOCO, ALTURA_BLOCO, cor.darker());
    }
    
    //desenha o fundo a grade e os blocos que ja estao pousados
    public void draw(EngineFrame engine) {
        engine.fillRectangle(posx, posy, colunas * LARGURA_BLOCO, linhas * ALTURA_BLOCO, COR_FUNDO);

        for (int linha = 0; linha < linhas; linha++) {
            for (int coluna = 0; coluna < colunas; coluna++) {
                Color cor = gradeDeCores[linha][coluna];
                if (cor != null) {
                    desenharBloco(engine, linha, coluna, cor);
                } else {
                    engine.drawRectangle(colunaParaX(coluna), linhaParaY(linha),
                            LARGURA_BLOCO, ALTURA_BLOCO, COR_GRADE);
                }
            }
        }

        engine.drawRectangle(posx, posy, colunas * LARGURA_BLOCO, linhas * ALTURA_BLOCO, COR_BORDA);
    }

  
    public int getLinhas() {
        return linhas;
    }

    public int getColunas() {
        return colunas;
    }

    public int getPosx() {
        return posx;
    }

    public int getPosy() {
        return posy;
    }
    public int getLargura() {
        return colunas * LARGURA_BLOCO;
    }

    public int getAltura() {
        return linhas * ALTURA_BLOCO;
    }
   
}




