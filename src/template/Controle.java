package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import java.util.List;

/**
 * @author Yoná Maia & Matheus Ubinha
 */

public class Controle {

    public static final double TEMPO_EMPILHAR = 30.0;
    public static final double TEMPO_DESEMPILHAR = 50.0;
    public static final int TAMANHO_INICIAL = 3;
    public static final int TAMANHO_MAXIMO = 8;
    
    public static final int PONTOS_POR_PELUCIA = 10;
    public static final int PONTOS_VITORIA = 70; 
    
    public static final double BONUS_TEMPO = 5.0;   // segundos ganhos a cada pelúcia empilhada certa

    public enum Fase { AGUARDANDO, EMPILHANDO, DESEMPILHANDO, FIM_DE_JOGO, VITORIA }

    public enum ResultadoSoltura {
        SEM_PELUCIA,          // a garra não estava segurando nada
        FORA_DA_ZONA,         // soltou fora da zona da pilha 
        ORDEM_ERRADA,         // zona reconheceu, mas não era a pelúcia da vez
        EMPILHADA,            // entrou na pilha
        SEQUENCIA_COMPLETA    // entrou na pilha e completou a sequência
    }

    private final Garra garra;
    private final ZonaDeEntrega zona;
    private final Cronometro cronometro = new Cronometro();

    private Pilha<Pelucia> pilha = new Pilha<>();   
    
    private SequenciaAlvo sequencia;                // ordem a seguir na rodada
    private Fase fase = Fase.AGUARDANDO;
    private Pelucia naGarra;                        // pelúcia que a garra está segurando (fase EMPILHANDO)
    private int rodada, pontos, erros, desempilhadas;

    public Controle( Garra garra, ZonaDeEntrega zona ) {
        this.garra = garra;
        this.zona = zona;
        
    }

    public void iniciar() {
        pilha = new Pilha<>();
        naGarra = null;
        rodada = 0;
        pontos = 0;
        erros = 0;
        novaRodada();
        
    }

    private void novaRodada() {
        rodada++;
        int tamanho = Math.min( TAMANHO_INICIAL + rodada - 1, TAMANHO_MAXIMO );
        sequencia = SequenciaAlvo.gerar( tamanho );
        desempilhadas = 0;
        fase = Fase.EMPILHANDO;
        cronometro.iniciar( TEMPO_EMPILHAR );
    }

    public void atualizar( double delta ) {
        if ( fase != Fase.EMPILHANDO && fase != Fase.DESEMPILHANDO ) {
            return;
        }
        cronometro.atualizar( delta );
        if ( cronometro.terminou() ) {
            naGarra = null;
            fase = Fase.FIM_DE_JOGO;
        }
    }

    public boolean tentarPegar( Pelucia pelucia ) {
        if ( fase != Fase.EMPILHANDO || naGarra != null || pelucia == null ) {
            return false;
        }
        if ( garra.tentarPegar( pelucia ) ) {
            naGarra = pelucia;
            return true;
        }
        return false;
    }

    public ResultadoSoltura soltar( double x, double y ) {
        if ( fase != Fase.EMPILHANDO || naGarra == null ) {
            return ResultadoSoltura.SEM_PELUCIA;
            
        }
        Pelucia solta = naGarra;
        naGarra = null;

        if ( !zona.reconhece( x, y ) ) {   // caiu na zona da pilha?
            return ResultadoSoltura.FORA_DA_ZONA;
            
        }
        if ( solta.getTipo() != sequencia.getEsperado() ) {  // era a pelúcia da vez?
            erros++;
            return ResultadoSoltura.ORDEM_ERRADA;
            
        }

        pilha.empilhar( solta ); //empilha e avança a sequência
        sequencia.avancar();

        if ( sequencia.concluida() ) {
            fase = Fase.DESEMPILHANDO;
            cronometro.iniciar( TEMPO_DESEMPILHAR ); //começa o desempilhar
            return ResultadoSoltura.SEQUENCIA_COMPLETA;
        }

        cronometro.acrescentar( BONUS_TEMPO );
        return ResultadoSoltura.EMPILHADA;
    }
    
    public Pelucia tentarDesempilhar() {
        
        if ( fase != Fase.DESEMPILHANDO ) {
            return null;
            
        }
        
        Pelucia topo = pilha.topo();
        
        if ( !garra.tentarPegar( topo ) ) {
            return null;
            
        }
        pilha.desempilhar();
        desempilhadas++;

        if ( pilha.estaVazia() ) {
            pontos += PONTOS_POR_PELUCIA * sequencia.tamanho();
            
            if ( pontos >= PONTOS_VITORIA ) {
                fase = Fase.VITORIA;
                
            } else {
                novaRodada();
            }
        }
        return topo;
    }


    public Fase getFase() { return fase; }
    public int getPontos() { return pontos; }
    public int getRodada() { return rodada; }
    public int getErros() { return erros; }
    public double getTempoRestante() { return cronometro.getRestante(); }
    public Pelucia getPeluciaNaGarra() { return naGarra; }
    public SequenciaAlvo getSequencia() { return sequencia; }
    public int getDesempilhadas() { return desempilhadas; }
    public ZonaDeEntrega getZona() { return zona; }
    public Garra getGarra() { return garra; }


    public int getTamanhoPilha() { return pilha.tamanho(); }

 
    public List<Pelucia> getPilha() { return pilha.paraLista(); }


    public void desenharPilha( EngineFrame e, double cx, double yBase, double altura ) {
        
        List<Pelucia> itens = pilha.paraLista();

        // desenha do topo para a base: cada pelúcia fica na frente da de cima e todos os rostos aparecem
        for ( int i = itens.size() - 1; i >= 0; i-- ) {
            itens.get( i ).getTipo().desenhar( e, cx, yBase - ( i + 0.5 ) * altura, Pelucia.Raio );

        }
    }

    public void desenharHud( EngineFrame e, double xInfo, double yInfo, double wInfo,
                             double xOrdem, double yOrdem, double wOrdem ) {

        e.setFontSize( 18 );
        e.drawText( pontos + " pts", xInfo, yInfo, EngineFrame.GOLD );

        if ( sequencia == null ) {
            return;
        }

        // barra de tempo
        cronometro.desenhar( e, xInfo, yInfo + 34, wInfo, 20 );

        boolean empilhando = !sequencia.concluida();
        sequencia.desenhar( e, xOrdem, yOrdem, wOrdem, !empilhando, empilhando ? sequencia.getProgresso() : desempilhadas );

    }

}