package atlassharedclasses;

public class Region {
	private Point point1;
	private Point point2;
	
	public Region(double a, double b, double c, double d) {
		this.point1 = new Point(a,b);
		this.point2 = new Point(c,d);
	}
	
	public Region(Point p1, Point p2) {
		this.point1 = p1;
		this.point2 = p2;
	}

	public double width() {
		return Math.abs(point1.getX() - point2.getX());
	}

	public double height() {
		return Math.abs(point1.getY() - point2.getY());		
	}
	
	public double left() {
		return Math.min(point1.getX(), point2.getX());
	}
	
	public double bottom() {
		return Math.min(point1.getY(), point2.getY());
	}
	
	public Point minCoord() {
		return new Point(this.left(), this.bottom());
	}
	
	public double right() {
		return Math.max(point1.getX(), point2.getX());
	}
	
	public double top() {
		return Math.max(point1.getY(), point2.getY());
	}

	public static Region squareAroundPoint(Point p, double size) {
		double halfsize = size/2;
		double l = p.getX() - halfsize;
		double r = p.getX() + halfsize;
		double b = p.getY() - halfsize;
		double t = p.getY() + halfsize;
		return new Region(new Point(l,b), new Point(r,t)); 
	}
	
	public String pointsListForObstacleFile() {
		double x1 = left();
		double x2 = right();
		double y1 = bottom();
		double y2 = top();
		return x1 + "," + y2 + ":" + x1 + "," + y1 + ":" + x2 + "," + y1 + ":" + x2 + "," + y2;
	}
	
	public String toString() {
		return super.toString() + point1.toString() + "-" + point2.toString();
	}

	public boolean contains(Point coord) {
		double x = coord.getX();
		double y = coord.getY();
		return (x > this.left() && x < this.right() && y > this.bottom() && y < this.top());
	}
}
