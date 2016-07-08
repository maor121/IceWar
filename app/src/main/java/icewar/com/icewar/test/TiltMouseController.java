/* Shahar Kosti			021639968
   Maor Shliefer		305206898 */

package icewar.com.icewar.test;

import android.graphics.Point;

import icewar.com.icewar.game.GameRunner;
import icewar.com.icewar.game.World;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class TiltMouseController implements MouseListener, MouseMotionListener {

	GameRunner runner;
	Point lastPoint;
	
	public TiltMouseController(GameRunner runner) {
		this.runner = runner;
	}

	public void mouseDragged(MouseEvent e) {
		World world = runner.getWorld();
		if (world != null) {
			Point newPoint = e.getPoint();
			
			int dX = newPoint.x - lastPoint.x;
			int dY = newPoint.y - lastPoint.y;
			
			world.tiltSurface(dY, -dX);
			
			lastPoint = newPoint;
		}
	}

	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseClicked(MouseEvent e) {
		
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		lastPoint = e.getPoint();
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
