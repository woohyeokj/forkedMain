package snakeladder.game;

import ch.aplu.jgamegrid.Actor;

public class Die extends Actor
{
  private NavigationPane np;
  private int nb;
  private int stepsToMove;

  Die(int nb, NavigationPane np, int stepsToMove)
  {
    super("sprites/pips" + nb + ".gif", 7);
    this.nb = nb;
    this.np = np;
    this.stepsToMove = stepsToMove;
  }

  public void act()
  {
    showNextSprite();
    if (getIdVisible() == 6)
    {
      setActEnabled(false);
      np.startMoving(stepsToMove);
    }
  }

}
