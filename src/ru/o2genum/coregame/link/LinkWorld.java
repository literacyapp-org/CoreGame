package ru.o2genum.coregame.link;

import java.util.*;

import ru.o2genum.coregame.framework.*;
import ru.o2genum.coregame.framework.Input.KeyEvent;
import ru.o2genum.coregame.framework.Input.TouchEvent;
import ru.o2genum.coregame.link.Utility.LinkItemType;
import ru.o2genum.coregame.link.Utility.LinkWayTurn;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

/* I should have used pools for my objects not to make garbage
 * collector angry. As it freezes the game sometimes, 
 * I avoided some object creations. However, it doesn't help.
 */

public class LinkWorld {
	Random random = new Random();
	Game game;
	// In this case ArrayList is better than LinkedList:
	// list will never be resized.

	Map<Point, LinkItem> map = new HashMap<Point, LinkItem>();

	private float time = 0.0F; // in seconds

	public enum GameState {
		Ready, Running, Paused, GameOver
	}

	public GameState state = GameState.Ready;

	// Sounds
	// Dot collides with core
	Sound coreHurt;
	Sound coreHealth;
	Sound coreShield;
	// Dot collides with shield
	Sound shieldCollision;

	Sound gameOver;

	public LinkWorld(Game game) {
		this.game = game;
		loadSounds();
	}

	private void loadSounds() {
		Audio a = game.getAudio();
		coreHurt = a.newSound("core_hurt.wav");
		coreHealth = a.newSound("core_health.wav");
		coreShield = a.newSound("core_shield.wav");
		shieldCollision = a.newSound("shield_collision.wav");
		gameOver = a.newSound("game_over.wav");
	}

	// Restart the game
	public void renew() {
		map.clear();
		time = 0.0F;
		state = GameState.Ready;
		generateNewLinkGame();
	}

	public void update(float deltaTime) {
		if (state == GameState.Ready)
			updateReady(deltaTime);
		if (state == GameState.Running)
			updateRunning(deltaTime);
		if (state == GameState.Paused)
			updatePaused(deltaTime);
		if (state == GameState.GameOver)
			updateGameOver(deltaTime);
	}

	private LinkItem previouseSelected;

	public LinkItem getPreviouseSelected() {
		return previouseSelected;
	}

	private LinkItem currectSelected;

	public LinkItem getCurrectSelected() {
		return currectSelected;
	}

	private LinkWay linkWay;

	private void doInput() {
		if (game.getInput().isTouchDown()) {
			int touchX = game.getInput().getTouchX();
			int touchY = game.getInput().getTouchY();
			Canvas c = game.getGraphics().getCanvas();
			int bitmapWidth = (c.getWidth() - (Utility._edge + (Utility._horizontalLinkItemCount)
					* Utility._edge))
					/ Utility._horizontalLinkItemCount;

			int x = touchX / (Utility._edge + bitmapWidth);
			int y = touchY / (Utility._edge + bitmapWidth);

			Iterator<LinkItem> iterator = map.values().iterator();
			while (iterator.hasNext()) {
				LinkItem linkitem = iterator.next();

				if (linkitem.GetIndex().x == x && linkitem.GetIndex().y == y) {
					linkitem.isSelect = true;
					if (previouseSelected == null) {
						previouseSelected = linkitem;

						coreHurt.play(4);
					} else {
						if (previouseSelected != null) {
							linkWay = FindWay(previouseSelected.GetIndex(),
									linkitem.GetIndex());
						}
						if (linkWay.getWaySize() >= 2) {
							currectSelected = linkitem;

						} else if (previouseSelected != linkitem) {
							previouseSelected = linkitem;

							coreHurt.play(4);
						}
					}
				} else {
					linkitem.isSelect = false;
				}
			}

			if (linkWay != null && linkWay.getWaySize() >= 2) {
				map.remove(previouseSelected.GetIndex());
				map.remove(currectSelected.GetIndex());
				previouseSelected = null;
				currectSelected = null;
				linkWay = null;
				gameOver.play(4);
			}

			if (previouseSelected != null) {
				previouseSelected.isSelect = true;
			}
		} else {
		}
	}

	public void CleanLinkedPoint() {
		if (linkWay != null && linkWay.getWaySize() >= 2) {
			map.remove(previouseSelected.GetIndex());
			map.remove(currectSelected.GetIndex());
			previouseSelected = null;
			currectSelected = null;
			linkWay = null;
		}
	}

	private void updateReady(float deltaTime) {
		if (checkTouchUp() || checkMenuUp())
			state = GameState.Running;
	}

	private boolean checkTouchUp() {
		for (TouchEvent event : game.getInput().getTouchEvents()) {
			if (event.type == TouchEvent.TOUCH_UP)
				return true;
		}
		return false;
	}

	private boolean checkMenuUp() {
		for (KeyEvent event : game.getInput().getKeyEvents()) {
			if (event.keyCode == android.view.KeyEvent.KEYCODE_MENU) {
				if (event.type == KeyEvent.KEY_UP)
					return true;
			}

		}
		return false;
	}

	private void updatePaused(float deltaTime) {
		if (checkTouchUp() || checkMenuUp())
			state = GameState.Running;
	}

	private void updateGameOver(float deltaTime) {
		if (checkTouchUp() || checkMenuUp())
			renew();
	}

	private void updateRunning(float deltaTime) {
		checkTouchUp(); // Just to clear touch event buffer

		if (checkMenuUp())
			state = GameState.Paused;

		countTime(deltaTime);

		doInput();
	}

	private void generateNewLinkGame() {
		int itemTypeCount = 10;

		while (map.size() < Utility._horizontalLinkItemCount
				* Utility._verticalLinkItemCount) {
			if (itemTypeCount < 0) {
				itemTypeCount = 10;
			}
			LinkItemType itemType = getLinkItemType(itemTypeCount);

			for (int t = 0; t < 2; t++) {

				while (true) {

					int x = random.nextInt(Utility._horizontalLinkItemCount);
					int y = random.nextInt(Utility._verticalLinkItemCount);
					Point point = new Point(x, y);
					if (!map.containsKey(point)) {
						map.put(point, new LinkItem(itemType, point, game
								.getBitmap().get(itemTypeCount)));
						break;
					}
				}
			}

			itemTypeCount--;
		}

	}

	private LinkWay FindWay(Point start, Point end) {
		LinkWay result = new LinkWay();

		if (map.get(start).GetLinkItemType() == map.get(end).GetLinkItemType()) {
			FindWay(start, end, result, -1, LinkWayTurn.none);
		}

		return result;
	}

	private boolean FindWay(Point start, Point end, LinkWay linkway,
			int turnCount, LinkWayTurn wayTurn) {
		linkway.AddPointToEnd(start);

		Iterator<Point> iterator = linkway.getWayPoint().iterator();

		while (iterator.hasNext()) {
			Point point = iterator.next();
		}

		if (turnCount > 2) {
			linkway.RemovePointFromEnd();
			return false;
		}

		if (start.x == end.x && start.y == end.y) {
			return true;
		}

		if (start.x < -1 || start.x > Utility._horizontalLinkItemCount + 1
				|| start.y < -1 || start.y > Utility._verticalLinkItemCount + 1) {
			linkway.RemovePointFromEnd();
			return false;
		}

		if (map.containsKey(start) && wayTurn != LinkWayTurn.none) {
			linkway.RemovePointFromEnd();
			return false;
		}

		Point next;

		if (wayTurn != LinkWayTurn.down) {
			next = GenerateNextPoint(start, LinkWayTurn.up);

			if (FindWay(next, end, linkway,
					wayTurn == LinkWayTurn.up ? turnCount : turnCount + 1,
					LinkWayTurn.up)) {
				return true;
			}
		}
		if (wayTurn != LinkWayTurn.left) {

			next = GenerateNextPoint(start, LinkWayTurn.right);

			if (FindWay(next, end, linkway,
					wayTurn == LinkWayTurn.right ? turnCount : turnCount + 1,
					LinkWayTurn.right)) {
				return true;
			}
		}
		if (wayTurn != LinkWayTurn.up) {
			next = GenerateNextPoint(start, LinkWayTurn.down);

			if (FindWay(next, end, linkway,
					wayTurn == LinkWayTurn.down ? turnCount : turnCount + 1,
					LinkWayTurn.down)) {
				return true;
			}
		}

		if (wayTurn != LinkWayTurn.right) {

			next = GenerateNextPoint(start, LinkWayTurn.left);
			if (FindWay(next, end, linkway,
					wayTurn == LinkWayTurn.left ? turnCount : turnCount + 1,
					LinkWayTurn.left)) {
				return true;
			}
		}

		linkway.RemovePointFromEnd();
		return false;
	}

	private Point GenerateNextPoint(Point point, LinkWayTurn wayTurn) {
		Point next = new Point(point.x, point.y);
		switch (wayTurn) {
		case down:
			next = new Point(point.x, point.y + 1);
			break;
		case up:

			next = new Point(point.x, point.y - 1);
			break;
		case left:

			next = new Point(point.x - 1, point.y);
			break;
		case right:

			next = new Point(point.x + 1, point.y);
			break;
		default:
			break;
		}

		return next;
	}

	private LinkItemType getLinkItemType(int type) {
		switch (type) {
		case 0:
			return LinkItemType.T0;
		case 1:
			return LinkItemType.T1;
		case 2:
			return LinkItemType.T2;
		case 3:
			return LinkItemType.T3;
		case 4:
			return LinkItemType.T4;
		case 5:
			return LinkItemType.T5;
		case 6:
			return LinkItemType.T6;
		case 7:
			return LinkItemType.T7;
		case 8:
			return LinkItemType.T8;
		case 9:
			return LinkItemType.T9;
		case 10:
			return LinkItemType.T10;

		default:
			return LinkItemType.T0;
		}
	}

	private void countTime(float deltaTime) {
		time += deltaTime;
	}

	public String getTime() {
		int seconds = (int) time;
		int minutes = seconds / 60;
		seconds %= 60;
		String result = "";
		if (minutes > 0)
			result += minutes + ":";
		result += String.format("%02d", seconds);
		return result;
	}

	public LinkWay getLinkWay() {
		return linkWay;
	}
}
