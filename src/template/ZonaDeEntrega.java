package template;

import br.com.davidbuzatto.jsge.collision.CollisionUtils;
import br.com.davidbuzatto.jsge.geom.Rectangle;

public class ZonaDeEntrega {
    private final Rectangle area;

    public ZonaDeEntrega( double x, double y, double largura, double altura ) {
        this.area = new Rectangle( x, y, largura, altura );
    }

    public boolean reconhece( double px, double py ) {
        return CollisionUtils.checkCollisionPointRectangle( px, py, area );
    }

    public boolean reconheceColuna( double px ) {
        return reconhece( px, area.y + area.height / 2 );
    }

    public double getCentroX() {
        return area.x + area.width / 2;
    }

    public Rectangle getArea() { return area; }

}
