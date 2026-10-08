

package template;

import java.awt.Color;
import java.util.Random;

public enum TipoPeca {

    I(new int[][]{ {0, -1}, {0, 0}, {0, 1}, {0, 2} },   new Color(0, 190, 230),  true),
    O(new int[][]{ {0, 0}, {0, 1}, {1, 0}, {1, 1} },    new Color(240, 200, 0),  false),
    T(new int[][]{ {-1, 0}, {0, -1}, {0, 0}, {0, 1} },  new Color(160, 60, 210), true),
    S(new int[][]{ {-1, 0}, {-1, 1}, {0, -1}, {0, 0} }, new Color(40, 190, 70),  true),
    Z(new int[][]{ {-1, -1}, {-1, 0}, {0, 0}, {0, 1} }, new Color(220, 50, 50),  true),
    J(new int[][]{ {-1, -1}, {0, -1}, {0, 0}, {0, 1} }, new Color(40, 90, 220),  true),
    L(new int[][]{ {-1, 1}, {0, -1}, {0, 0}, {0, 1} },  new Color(240, 140, 0),  true);

    private final int[][] formato;
    private final Color cor;
    private final boolean podeGirar;   // a O nao gira (girar so faria ela "tremer")

    TipoPeca(int[][] formato, Color cor, boolean podeGirar) {
        this.formato = formato;
        this.cor = cor;
        this.podeGirar = podeGirar;
    }

    //Devolve uma copia do formato. Assim quando uma peca gira
    //ela muda a copia dela e nao estraga o formato original do tipo
    public int[][] getFormato() {
        int[][] copia = new int[formato.length][2];
        for (int i = 0; i < formato.length; i++) {
            copia[i][0] = formato[i][0];
            copia[i][1] = formato[i][1];
        }
        return copia;
    }

    public Color getCor() {
        return cor;
    }

    public boolean podeGirar() {
        return podeGirar;
    }

    /** Sorteia um dos 7 tipos. values() devolve todas as constantes do enum. */
    public static TipoPeca sortear(Random sorteio) {
        TipoPeca[] todos = values();
        return todos[sorteio.nextInt(todos.length)];
    }

}
