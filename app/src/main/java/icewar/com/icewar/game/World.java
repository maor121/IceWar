/* Shahar Kosti			021639968
   Maor Shliefer		305206898 */

package icewar.com.icewar.game;

import android.content.Context;
import android.graphics.PointF;

import com.threed.jpct.Object3D;

import icewar.com.icewar.core.*;
import icewar.com.icewar.util.Timer;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for:
 *  - Updating the game objects in the correct other
 *  - Checking for collisions (players and pillars)
 *  - Applying the correct player state
 *  - Drawing the world: surface, players, pillars and crown
 */
public class World {

	//Physics
	//Restitution - value between 1 and 0. 1 = elastic, 0 = perfect inelastic collision
    private static final float PLAYER_PLAYER_COLLISION_RESTITUION_COEFFICIENT = 0.5f;
    private static final float PLAYER_PILLAR_COLLISION_RESTITUTION_COEFFICIENT = 1;
    private static final float KINETIC_FRICTION_COEFFICIENT = 0.03f; //0.15 is rubber on ice
	private static final float STATIC__FRICTION_COEFFICIENT = 0.1f;
	
	
	//Game
    private static final int NUM_OF_PILLARS = 24;
    private static final float PLAYER_MASS = 3;
    private static final float PLAYER_RADIUS = 0.7f;
    private static final float SURFACE_RADIUS = 6.6f;
    private static final float PILLAR_MASS = 10;
    private static final float PILLAR_RADIUS = 0.1f;

    private final Renderer renderer;
    private final Timer timer = new Timer();
    private final PhysicsEngine physicsEngine = new PhysicsEngine();
    private boolean isRunning;

    private Surface surface;
    private final List<Player> pillars = new ArrayList<>();
    private final List<GameObject> gameObjects = new ArrayList<>();

    private List<PlayerController> players = new ArrayList<>();

    private Player winner;
    private Player crown;
    
    public World(Renderer renderer) {
        this.renderer = renderer;
        this.winner = null;
        isRunning = true;
    }

    /** Pause the timer */
    public void pause() {
    	isRunning = false;
    }
    public void run() {
    	isRunning = true;
    	this.timer.restart();
    }

    /** Update object positions, check for collisions and draw */
    public void update() {
    	
    	//if isn't running, just draw, don't update positions etc.
    	if (isRunning) {
	        final double elapsedSeconds = timer.elapsedSeconds();
	
	        this.surface.update(elapsedSeconds);
	
	        final float tiltY = surface.getOrientation().y();
	        final float tiltZ = surface.getOrientation().z();
	
	        for (Player pillar : pillars) {
	            pillar.setOrientation(0, tiltY, tiltZ);
	        }
	
	        for (PlayerController playerController : players) {
	        	final Player player = playerController.getPlayer();
	
	            checkIsOnSurface(player);
	
	            //Add gravity
	            Vector gravity = new Vector(0, -9.8f*player.getMass(), 0);
	            player.addForce(gravity);
	
	            applySurfaceForces(tiltY, tiltZ, playerController, player);
	
	            physicsEngine.update(player, elapsedSeconds);
	
	            collideWithPillars(player);
	
	            collideWithPlayers(playerController, player);
	        }
	
	        for (Player pillar : pillars) {
	            physicsEngine.update(pillar, elapsedSeconds);
	        }
        
    	}
        
        for (GameObject gameObject : gameObjects) {
            renderer.renderGameObject(gameObject);
        }
        
        //draw crown for winner if exists
        if (winner != null) {
            showCrown();
        }
        
        timer.restart();
    }

    /** Used in update() */
    private void checkIsOnSurface(Player player) {
        //Check if player is on surface
        double playerSurfaceOverlap;
        playerSurfaceOverlap = surface.overlappingDistance(player);
        if (playerSurfaceOverlap < player.getRadius() / 2) {
            if (player.isOnSurface()) {
                SoundEngine.playFallEffect();
            }
            player.setIsOnSurface(false);
        }
    }

    /** Used in update() */
    private void applySurfaceForces(float tiltY, float tiltZ, PlayerController playerController, Player player) {
        if (player.isOnSurface()) { //if player is on surface

            //add slide forces and update
            Vector surfaceForce = computeSurfaceForces(player);
            player.setForce(surfaceForce);
            playerController.update(this);

            //tilt player with surface
            Vector playerOrientation = player.getOrientation();
            player.setOrientation(playerOrientation.x(), tiltY, tiltZ);
        }
        else
            playerController.update(this); //not really neccessary?
    }

    /** Used in update() */
    private void collideWithPlayers(PlayerController playerController, Player player) {
        for (PlayerController otherController : players) {
            if (playerController != otherController) {

                Player other = otherController.getPlayer();

                float overlapdist = player.overlappingDistance(other);

                if (overlapdist > 0) {
                    //collision of two players
                    PointF center1 = player.getCenter();
                    PointF center2 = other.getCenter();

                    Vector retractDirection = new Vector(center2.x - center1.x, 0, center2.y - center1.y);

                    player.retract(retractDirection, overlapdist);

                    collision(player, other, PLAYER_PLAYER_COLLISION_RESTITUION_COEFFICIENT);
                }
            }
        }
    }

    /** Used in update() */
    private void collideWithPillars(Player player) {
        for (Player pillar : pillars) {
            if (!pillar.isOnSurface()) continue;

            final float overlapdist = player.overlappingDistance(pillar);
            if (overlapdist > 0) {
                pillar.setForce(new Vector(0, 1e5f, 0));
                pillar.setIsOnSurface(false);
                SoundEngine.playPillar();

                //collision of player & pillar
                PointF center1 = player.getCenter();
                PointF center2 = pillar.getCenter();

                Vector retractDirection = new Vector(center2.x - center1.x, 0, center2.y - center1.y);

                player.retract(retractDirection, overlapdist);
                collision(player, pillar, PLAYER_PILLAR_COLLISION_RESTITUTION_COEFFICIENT);
            }
        }
    }

    /** Show a crown above the winning player */
    private void showCrown() {
        Vector winnerPos = winner.getPosition();
        Vector orientation = winner.getOrientation();

        crown.setPosition( new Vector( winnerPos.x(), winnerPos.y() + 1.3f, winnerPos.z() ) );
        crown.setOrientation(0, orientation.y(), orientation.z());
        renderer.renderGameObject(crown);
    }

    /** Show only the final winner */
    public void showFinalWinner() {
        if (winner == null) {
            return;
        }
        winner.setPosition(Vector.Zero);
        winner.setOrientation(winner.getOrientation().x() - 0.5f, 0, 0);
        renderer.renderGameObject(winner);

        showCrown();
    }

    /**
     * Return game players.
     */
    public List<PlayerController> getPlayers() {
        return players;
    }

    public PlayerController getPlayer(int id) {
        for (PlayerController player : players) {
            if (player.getId() == id) {
                return player;
            }
        }
        return null;
    }

    /**
     * Used to disintigrate the forces active on the player into surface forces. <br />
     * For example: gravity will become Normal, Friction, and Push. <br />
     * Other forces are taken into account as well.
     * Also, if no forces are active on the player, but he has velocity, this method
     * will calculate the friction.
     */
    private Vector computeSurfaceForces(Player p) {
        final float tiltY = surface.getOrientation().y();
        final float tiltZ = surface.getOrientation().z();

    	Vector velocity = p.getVelocity();
    	Vector F = p.getForce();
    	
    	float N/*Normal*/, Ffmax/*Max static friction*/;
    	
    	float yRadians = (float)Math.toRadians(tiltY);
    	float zRadians = (float)Math.toRadians(tiltZ);
    	
    	/*
    	 * rotates a 2D point around the origin (x=0, y=0) using the formula

			xnew = x * cos(angle) - y * sin(angle)
			ynew = y * cos(angle) + x * sin(angle)
			
			point (1,0)
    	 */
    	//Convert F to new axis, the surface plane creates the new axis
    	Vector zAxis = new Vector((float)-Math.sin(zRadians), 1 * (float)Math.cos(zRadians),0);
    	Vector yAxis = new Vector(0, 1 * - (float)Math.sin(yRadians), 1* (float)Math.cos(yRadians));
    	
    	Matrix axisTranslationMatrix = Matrix.createAxisTranslation(zAxis, yAxis);
    	Vector newF = axisTranslationMatrix.mul(F);
    	
    	//Take the horizontal force as the Pushing force
    	Vector Fhorizontal = new Vector(newF.x(), 0, newF.z());
    	Vector Ffsign, Ff;
    	//Take the vertical force as the Normal
    	N = -newF.y();
    	
    	//If horizontal force is greater than max static friction move the object
    	Ffmax = STATIC__FRICTION_COEFFICIENT * N;
    	if ( (Ffmax < Fhorizontal.size()) || (velocity.size() != 0) ){
    		//Calculate friction force
    		Ffsign = velocity.size() == 0 ? Fhorizontal.normalize().reverse() : velocity.normalize().reverse();
    		Ff = Ffsign.mul( KINETIC_FRICTION_COEFFICIENT * N );
    		
    		//Add friction to horizontal force
    		Fhorizontal = Fhorizontal.add(Ff);
    	}
    	else 
    		//horizontal force is not greater than static friction. Don't move
    		Fhorizontal = new Vector(0,0,0);
    	
    	return Fhorizontal;
    }
    
    /***
     * Perform collision between two players. Changes the players velocities accordingly <br>
     * Also used for collision between player and pillar.
     * <br>
     * if CR = 1 it's elastic collision <br>
     * if CR = 0 it's perfect inelastic collision <br>
     * @see <a href='http://en.wikipedia.org/wiki/Coefficient_of_restitution'>http://en.wikipedia.org/wiki/Coefficient_of_restitution</a>
     * @param p1 player 1
     * @param p2 player 2
     * @param CR coefficient of restitution (value between 0 and 1)
     */
    private void collision(Player p1, Player p2, float CR) {
    	
    	float m1, m2; //player masses
    	Vector u1, u2; //player velocities BEFORE collision
    	Vector v1, v2; //player velocities AFTER collision
    	
    	m1 = p1.getMass();
    	m2 = p2.getMass();
    	u1 = p1.getVelocity();
    	u2 = p2.getVelocity();
    	
    	//v1 = ( m1*u1 + m2*u2 + m2*CR*(u2-u1) ) / (m1+m2)
    	v1 = u1.mul(m1).add(u2.mul(m2)).add( u2.subtract(u1).mul(m2 * CR) ).div(m1+m2);
    	v2 = u2.mul(m2).add(u1.mul(m1)).add( u1.subtract(u2).mul(m1 * CR) ).div(m1+m2);
    	
    	p1.setVelocity( v1 );
    	p2.setVelocity(v2);
    }
    
    /**
     * Tilt the surface by dY and dZ degrees.
     * @param dY the degrees to tilt the Y axis
     * @param dZ the degrees to tilt the Z axis
     */
    public void tiltSurface(float dY, float dZ) {
        final Vector orientation = this.surface.getOrientation();
        this.surface.setOrientation(orientation.add(new Vector(0, dY, dZ)));
    }
    
    /**
     * load surface model (*.3ds file)
     * @param path string path
     * @throws Exception when model cannot be loaded
     */
    public void loadSurface(Context ctx, String path) throws Exception {
        final Object3D model = this.renderer.loadModel3DS(ctx, path);
        if (model == null) {
            throw new Exception("Cannot load surface model");
        }
        this.surface = new Surface(model, SURFACE_RADIUS);
        this.gameObjects.add(this.surface);
    }

    /**
     * load pillars model (*.3ds file)
     * @param path string path
     * @throws Exception when model cannot be loaded
     */
    public void loadPillars(Context ctx, String path) throws Exception {
        final Object3D model = this.renderer.loadModel3DS(ctx, path);
        if (model == null) {
            throw new Exception("Cannot load pillar model");
        }

        float num = (float) (2*Math.PI)/NUM_OF_PILLARS;
        float EDGE = surface.getRadius();
        for (double angle=0; angle < 2*Math.PI && pillars.size() < NUM_OF_PILLARS; angle += num) {
            Player pillar = new Player(model, PILLAR_MASS, PILLAR_RADIUS);
            pillar.setPosition(new Vector((float)Math.cos(angle)*EDGE, 0, (float)Math.sin(angle)*EDGE));
            this.gameObjects.add(pillar);
            this.pillars.add(pillar);
        }
    }

    /**
     * load player model (*.3ds file) and add it to the game <br />
     * for loading numerous players, call loadPlayer numerous times.
     * @param path string path
     * @throws Exception when model cannot be loaded
     */
    public PlayerController loadPlayer(Context ctx, String path, int id) throws Exception {
        final Object3D model = this.renderer.loadModel3DS(ctx, path);
        if (model == null) {
            throw new Exception("Cannot load player model");
        }

        Player gamePlayer = new Player(model, PLAYER_MASS, PLAYER_RADIUS);
        gameObjects.add(gamePlayer);

        PlayerController player = (id == 1) ?
                new KeyboardPlayerController(gamePlayer) :
                new OpponentPlayerController(gamePlayer);

        player.setId(id);
        this.players.add(player);
        return player;
    }
    
    /**
     * load crown model (*.3ds file)
     * @param path string path
     * @throws Exception when model cannot be loaded
     */
    public void loadCrown(Context ctx, String path) throws Exception {
    	final Object3D model = this.renderer.loadModel3DS(ctx, path);
        if (model == null) {
            throw new Exception("Cannot load crown model");
        }

        crown = new Player(model, 1, PLAYER_RADIUS);
    }

    /**
     * Declare a player as the winner
     * @param player
     */
    public void setWinner(Player player) {
    	this.winner = player;
    }

}
