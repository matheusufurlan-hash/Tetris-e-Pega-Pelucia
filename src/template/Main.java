package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import java.awt.Color;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;



/**
 * Modelo de projeto básico da JSGE.
 * 
 * JSGE basic project template.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class Main extends EngineFrame {
    
    private enum Estado { INICIO, JOGANDO, PAUSADO, FIM }
    
    private static final int LINHAS=20;
    private static final int COLUNAS=10;
    private static final int TAMANHO_FILA=3; // quantidade de pecas proximas que ficam visiveis
    private static final int BLOCO_PREVIA=16;  // tamanho em pixel dos quadrados pequenos na fila
    
    private static final double INTERVALO_INICIAL = 0.5; // segundos/linha no nivel 1
    private static final double REDUCAO_POR_NIVEL = 0.04; 
    private static final double INTERVALO_MINIMO = 0.08; 
    private static final int LINHAS_POR_NIVEL = 10; 
    
    //pontos pelas linhas removidas de uma unica vez (indice = quantidade de linhas) x nivel;
    private static final int[] PONTOS_POR_LINHAS = {0, 100, 300, 500, 800};
    
    private static final Color COR_TEXTO = new Color(40, 40, 50);
    private static final Color COR_TEXTO_FRACO = new Color(120, 120, 130);
    private static final Color COR_CAIXA = new Color(235, 235, 240);
    private static final Color COR_DESTAQUE = new Color(40, 90, 220);
    private static final Color COR_SOBREPOSICAO = new Color(0, 0, 0, 160);
    
    private Tabuleiro tabuleiro;
    private Peca peca;
    private Queue<TipoPeca> proximas; // a fila das pecas que estao de proxima
    private Random sorteio;
    private Estado estado;
    
    private int pontos;
    private int linhasFeitas;
    private int nivel;
    
    
    public Main() {
        
        
        super(
            800,                 // largura                      / width
            450,                 // algura                       / height
            "Tetris",      // título                       / title
            60,                  // quadros por segundo desejado / target FPS
            true,                // suavização                   / antialiasing
            false,               // redimensionável              / resizable
            false,               // tela cheia                   / full screen
            false,               // sem decoração                / undecorated
            false,               // sempre no topo               / always on top
            false                // fundo invisível              / invisible background
        );
        
    }
    
    /**
     * Cria o mundo do jogo.
     * Esse método executa apenas uma vez durante a inicialização da engine.
     * 
     * Creates the game world.
     * This method runs just one time during engine initialization.
     */
    @Override
    public void create() {
        sorteio = new Random();
        novoJogo();
        estado = Estado.INICIO;     
    }
    // Zera tudo: o tabuleiro fica vazio, a fila fica cheia, placar fica zerado e a primeira peca
    private void novoJogo(){
        // Tabuleiro no centro da Janela
        int posx = (getScreenWidth() - COLUNAS*Tabuleiro.LARGURA_BLOCO )/2;
        int posy = (getScreenHeight() - LINHAS*Tabuleiro.ALTURA_BLOCO )/2;
        tabuleiro = new Tabuleiro(LINHAS, COLUNAS, posx, posy);
        
        proximas = new LinkedList<>();
        for (int i = 0; i < TAMANHO_FILA; i++){
            proximas.offer(TipoPeca.sortear(sorteio)); //entra no fim da fila;
        }
        
        pontos = 0;
        linhasFeitas = 0;
        nivel = 1;
        
        novaPeca();
    }
    private TipoPeca pegarProximaPeca(){
        TipoPeca tipo = proximas.poll(); // sai da frente da fila
        proximas.offer(TipoPeca.sortear(sorteio)); // entra no fim da fila
        return tipo;
    }
    
    //cria a proxima peca no topo. Se nao tiver mais espaco eh game over
    private void novaPeca(){
        peca = new Peca(tabuleiro, pegarProximaPeca(), COLUNAS / 2 - 1);
        if (!peca.cabe(0,0)){
            estado = Estado.FIM;
        }
    }
    //quanto mais niveis o jogador passar menor o intervalo e mais rapido a peca cai
    private double intervaloDoNivel(){
        return Math.max(INTERVALO_MINIMO, INTERVALO_INICIAL - (nivel - 1)*REDUCAO_POR_NIVEL);
    }
    //chamado quando a peca pousa: soma pontos, limpa as linhas e usa a proxima peca
    private void pecaPousou() {
        pontos += peca.getPontosQueda();

        int removidas = tabuleiro.limparLinhasCompletas();
        if (removidas > 0) {
            pontos += PONTOS_POR_LINHAS[removidas] * nivel;
            linhasFeitas += removidas;
            nivel = linhasFeitas / LINHAS_POR_NIVEL + 1;
        }

        novaPeca();
    }
    


    /**
     * Lê a entrada do usuário e atualiza o mundo do jogo.
     * Os métodos de entrada devem ser usados aqui.
     * Atenção: Você NÃO DEVE usar nenhum dos métodos de desenho da engine aqui.
     * 
     * Reads user input and update game world.
     * Input methods should be used here.
     * Warning: You MUST NOT use any of the engine drawing methods here.
     * 
     * @param delta O tempo passado, em segundos, de um quadro para o outro.
     * Time passed, in seconds, between frames.
     */
    @Override
    public void update(double delta) {
        switch (estado) {
            case INICIO:
                if (isKeyPressed(KEY_ENTER)) {
                    estado = Estado.JOGANDO;
                }
                break;

            case JOGANDO:
                if (isKeyPressed(KEY_P)) {
                    estado = Estado.PAUSADO;
                    break;
                }
                peca.update(delta, this, intervaloDoNivel());
                if (peca.estaFixada()) {
                    pecaPousou();
                }
                break;

            case PAUSADO:
                if (isKeyPressed(KEY_P)) {
                    estado = Estado.JOGANDO;
                }
                break;

            case FIM:
                if (isKeyPressed(KEY_ENTER)) {
                    novoJogo();
                    estado = Estado.JOGANDO;
                }
                break;
        }
    }

    
    /**
     * Desenha o mundo do jogo.
     * Todas as operações de desenho DEVEM ser feitas aqui.
     * 
     * Draws the game world.
     * All drawing related operations MUST be performed here.
     */
    @Override
    public void draw() {
        clearBackground(RAYWHITE);

        tabuleiro.draw(this);
        if (estado != Estado.FIM) {
            peca.draw(this);
        }

        desenharPainelEsquerdo();
        desenharFila();

        switch (estado) {
            case INICIO:
                desenharMensagem("TETRIS", "ENTER para começar");
                break;
            case PAUSADO:
                desenharMensagem("Pausado", "P para continuar");
                break;
            case FIM:
                desenharMensagem("Fim de jogo", "ENTER para jogar de novo");
                break;
            default:
                break;
        }
    }
    //titulo, placar e os controles a esquerda do tabuleiro
    private void desenharPainelEsquerdo() {
        int x = 40;

        drawText("TETRIS", x, 25, 36, COR_TEXTO);

        drawText("Pontos", x, 90, 14, COR_TEXTO_FRACO);
        drawText(String.valueOf(pontos), x, 108, 24, COR_TEXTO);

        drawText("Linhas", x, 150, 14, COR_TEXTO_FRACO);
        drawText(String.valueOf(linhasFeitas), x, 168, 24, COR_TEXTO);

        drawText("Nível", x, 210, 14, COR_TEXTO_FRACO);
        drawText(String.valueOf(nivel), x, 228, 24, COR_TEXTO);

        drawText("Controles", x, 290, 14, COR_TEXTO_FRACO);
        drawText("Esquerda/direita: mover", x, 312, 13, COR_TEXTO);
        drawText("Cima: girar", x, 330, 13, COR_TEXTO);
        drawText("Baixo: acelerar", x, 348, 13, COR_TEXTO);
        drawText("Espaço: derrubar", x, 366, 13, COR_TEXTO);
        drawText("P: pausar", x, 384, 13, COR_TEXTO);
    }
    //desenha a fila de proximas pecas, da frente (em cima) para o fim
    //percorre a fila com o for e nao remove nada, so olha os elementos em ordem
    private void desenharFila() {
        int x = tabuleiro.getPosx() + tabuleiro.getLargura() + 30;
        int y = 25;
        int larguraCaixa = 100;
        int alturaCaixa = 70;
        int espaco = 12;

        drawText("Próximas", x, y, 18, COR_TEXTO);
        drawText("(fila)", x, y + 22, 13, COR_TEXTO_FRACO);

        int yCaixa = y + 48;
        boolean primeira = true;

        for (TipoPeca tipo : proximas) {
            fillRectangle(x, yCaixa, larguraCaixa, alturaCaixa, COR_CAIXA);
            drawRectangle(x, yCaixa, larguraCaixa, alturaCaixa, primeira ? COR_DESTAQUE : COR_TEXTO_FRACO);
            desenharMiniatura(tipo, x, yCaixa, larguraCaixa, alturaCaixa);

            yCaixa += alturaCaixa + espaco;
            primeira = false;
        }

        drawText("A da frente (azul) entra", x, yCaixa + 4, 12, COR_TEXTO_FRACO);
        drawText("no jogo; uma nova entra", x, yCaixa + 20, 12, COR_TEXTO_FRACO);
        drawText("no fim da fila.", x, yCaixa + 36, 12, COR_TEXTO_FRACO);
    }
    //desenha a peca pequena dentro da caixa centralizada
    private void desenharMiniatura(TipoPeca tipo, int xCaixa, int yCaixa, int larguraCaixa, int alturaCaixa) {
        int[][] formato = tipo.getFormato();

        int menorLinha = formato[0][0], maiorLinha = formato[0][0];
        int menorColuna = formato[0][1], maiorColuna = formato[0][1];
        for (int[] bloco : formato) {
            menorLinha = Math.min(menorLinha, bloco[0]);
            maiorLinha = Math.max(maiorLinha, bloco[0]);
            menorColuna = Math.min(menorColuna, bloco[1]);
            maiorColuna = Math.max(maiorColuna, bloco[1]);
        }

        int larguraPeca = (maiorColuna - menorColuna + 1) * BLOCO_PREVIA;
        int alturaPeca = (maiorLinha - menorLinha + 1) * BLOCO_PREVIA;
        int x0 = xCaixa + (larguraCaixa - larguraPeca) / 2;
        int y0 = yCaixa + (alturaCaixa - alturaPeca) / 2;

        for (int[] bloco : formato) {
            int x = x0 + (bloco[1] - menorColuna) * BLOCO_PREVIA;
            int y = y0 + (bloco[0] - menorLinha) * BLOCO_PREVIA;
            fillRectangle(x, y, BLOCO_PREVIA, BLOCO_PREVIA, tipo.getCor());
            drawRectangle(x, y, BLOCO_PREVIA, BLOCO_PREVIA, tipo.getCor().darker());
        }
    }
    //escurece o tabuleiro e escreve duas linhas de texto central sobre ele
    private void desenharMensagem(String titulo, String subtitulo) {
        int x = tabuleiro.getPosx();
        int y = tabuleiro.getPosy();
        int largura = tabuleiro.getLargura();
        int altura = tabuleiro.getAltura();

        fillRectangle(x, y, largura, altura, COR_SOBREPOSICAO);

        int tamTitulo = 30;
        int tamSub = 14;
        drawText(titulo, x + (largura - measureText(titulo, tamTitulo)) / 2.0, y + altura / 2.0 - 30, tamTitulo, WHITE);
        drawText(subtitulo, x + (largura - measureText(subtitulo, tamSub)) / 2.0, y + altura / 2.0 + 10, tamSub, WHITE);
    }
    /**
     * Instancia a engine e a inicia.
     * 
     * Instantiates the engine and starts it.
     */
    
    
    public static void main( String[] args ) {
        new Main();
    }
    
    
}