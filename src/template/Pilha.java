package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class Pilha<T> {
    @FunctionalInterface
    public interface Desenhista<T> {
        void desenhar( EngineFrame engine, T item, double cx, double cy );

    }

    private static class No<T> {
        final T valor;
        final No<T> abaixo;

        No( T valor, No<T> abaixo ) {
            this.valor = valor;
            this.abaixo = abaixo;
        }
    }

    private No<T> noTopo;
    private int tamanho;

    public void empilhar( T valor ) {
        noTopo = new No<>( valor, noTopo );
        tamanho++;
    }

    public T desempilhar() {
        if ( estaVazia() ) {
            throw new NoSuchElementException( "A pilha está vazia." );
        }

        T valor = noTopo.valor;
        noTopo = noTopo.abaixo;
        tamanho--;
        return valor;
    }

    public T topo() {
        if ( estaVazia() ) {
            throw new NoSuchElementException( "A pilha está vazia." );
        }
        return noTopo.valor;
    }

    public boolean estaVazia() { return noTopo == null; }

    public int tamanho() { return tamanho; }

    public List<T> paraLista() {
        List<T> lista = new ArrayList<>();

        for ( No<T> no = noTopo; no != null; no = no.abaixo ) {
            lista.add( no.valor );

        }

        Collections.reverse( lista );
        return lista;
    }

    public void desenhar( EngineFrame e, double cx, double yBase, double largura, double altura,
                          int slots, Desenhista<T> desenhista ) {
        Color linha = new Color( 255, 255, 255, 60 );
        double esq = cx - largura / 2;

        for ( int i = 0; i < slots; i++ ) {
            e.drawRectangle( esq, yBase - ( i + 1 ) * altura, largura, altura, linha );
        }
        e.fillRectangle( esq - 6, yBase, largura + 12, 5, new Color( 150, 140, 210 ) );

        List<T> itens = paraLista();

        for ( int i = 0; i < itens.size(); i++ ) {
            desenhista.desenhar( e, itens.get( i ), cx, yBase - ( i + 0.5 ) * altura );
        }

        e.setFontSize( 13 );
        if ( estaVazia() ) {
            e.drawText( "vazia", cx - e.measureText( "vazia" ) / 2.0, yBase - altura / 2 - 7, EngineFrame.GRAY );

        } else {
            double yTopo = yBase - ( tamanho - 0.5 ) * altura;

            e.fillTriangle( esq - 14, yTopo - 6, esq - 14, yTopo + 6, esq - 4, yTopo, EngineFrame.GOLD );
            e.drawText( "TOPO", esq - 20 - e.measureText( "TOPO" ), yTopo - 7, EngineFrame.GOLD );
        }

    }

}
