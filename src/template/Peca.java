package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;


public class Peca {
    private static final double INTERVALO_RAPIDO = 0.04; //queda com a seta para baixo segurada

    //tenta o encaixe ao girar: {deslocLinha, deslocColuna}
    //se nao cabe no lugar tenta empurra-la um pouco ex: encostada na parede
    private static final int[][] TENTATIVAS_AO_GIRAR = {
        {0, 0}, {0, 1}, {0, -1}, {0, 2}, {0, -2}, {1, 0}
    };

    private final Tabuleiro tabuleiro; // referencia ao tab no main
    private final TipoPeca tipo;
    private int[][] formato; //copia do formato do tipo, muda ao girar

    private int linha;                   // ponto de referencia na grade
    private int coluna;
    private double tempoAcumulado;
    private boolean fixada;
    private int pontosQueda;             // pontos ganhos acelerando ou derrubando a peca



    public Peca(Tabuleiro tabuleiro, TipoPeca tipo, int coluna) {
        this.tabuleiro = tabuleiro;
        this.tipo = tipo;
        this.formato = tipo.getFormato();
        this.coluna = coluna;
        this.linha = linhaInicial();
        this.tempoAcumulado = 0;
        this.fixada = false;
        this.pontosQueda = 0;
    }
    //escolhe a linha inicial para o bloco mais alto da peca comecar na linha 0
    private int linhaInicial() {
        int menor = 0;
        for (int i = 0; i < formato.length; i++) {
            if (formato[i][0] < menor) {
                menor = formato[i][0];
            }
        }
        return -menor;
    }
    public boolean cabe(int deslocLinha, int deslocColuna) {
        return cabe(formato, deslocLinha, deslocColuna);
    }
    //verifica se cabe mas ao girar
    private boolean cabe(int[][] formatoTestado, int deslocLinha, int deslocColuna) {
        for (int i = 0; i < formatoTestado.length; i++) {
            int linhaBloco = linha + formatoTestado[i][0] + deslocLinha;
            int colunaBloco = coluna + formatoTestado[i][1] + deslocColuna;

            if (tabuleiro.estaPreenchido(linhaBloco, colunaBloco)) {
                return false;
            }
        }
        return true;
    }

    // le o teclado e faz a peca cair
    // intervalo: segundos entre uma descida e outra (diminui com o nivel)
    public void update(double delta, EngineFrame engine, double intervalo) {
        if (fixada) {
            return;
        }

        // Movimento lateral: isKeyPressed dispara uma vez por toque na tecla.
        if (engine.isKeyPressed(EngineFrame.KEY_LEFT) && cabe(0, -1)) {
            coluna--;
        }
        if (engine.isKeyPressed(EngineFrame.KEY_RIGHT) && cabe(0, 1)) {
            coluna++;
        }

        // Girar.
        if (engine.isKeyPressed(EngineFrame.KEY_UP)) {
            girar();
        }

        // Queda livre: desce ate onde der e fixa na hora.
        if (engine.isKeyPressed(EngineFrame.KEY_SPACE)) {
            while (cabe(1, 0)) {
                linha++;
                pontosQueda += 2;
            }
            fixar();
            return;
        }

        // Queda normal: o relogio junta delta ate passar do intervalo.
        // Com a seta para baixo segurada, o intervalo fica bem menor.
        boolean acelerando = engine.isKeyDown(EngineFrame.KEY_DOWN);
        double intervaloAtual = acelerando ? Math.min(INTERVALO_RAPIDO, intervalo) : intervalo;

        tempoAcumulado += delta;
        if (tempoAcumulado >= intervaloAtual) {
            tempoAcumulado -= intervaloAtual;
            if (cabe(1, 0)) {
                linha++;
                if (acelerando) {
                    pontosQueda += 1;
                }
            } else {
                fixar();
            }
        }
    }

    //gira a peca em 90 graus no ponto de ref
    // na tela (linha cresce para baixo) o bloco {linha, coluna} vai para {coluna, -linha}
    // se nao couber tenta as posicoes TENTATIVAS_AO_GIRAR, se nenhuma servir, nao gira
    public void girar() {
        if (!tipo.podeGirar()) {
            return;
        }

        int[][] girado = new int[formato.length][2];
        for (int i = 0; i < formato.length; i++) {
            girado[i][0] = formato[i][1];
            girado[i][1] = -formato[i][0];
        }

        for (int[] tentativa : TENTATIVAS_AO_GIRAR) {
            if (cabe(girado, tentativa[0], tentativa[1])) {
                formato = girado;
                linha += tentativa[0];
                coluna += tentativa[1];
                return;
            }
        }
    }
    //Fixa os blocos no tabuleiro. A partir disso quem desenha eh o tabuleiro.
    private void fixar() {
        for (int i = 0; i < formato.length; i++) {
            tabuleiro.ocupar(linha + formato[i][0], coluna + formato[i][1], tipo.getCor());
        }
        fixada = true;
    }

    public boolean estaFixada() {
        return fixada;
    }

    public int getPontosQueda() {
        return pontosQueda;
    }

    public void draw(EngineFrame engine) {
        for (int i = 0; i < formato.length; i++) {
            tabuleiro.desenharBloco(engine, linha + formato[i][0], coluna + formato[i][1], tipo.getCor());
        }
    }

}