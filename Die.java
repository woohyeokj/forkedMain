package snakeladder.game;

import ch.aplu.jgamegrid.Actor;
import java.util.ArrayList;

public class Die extends Actor
{
  private NavigationPane np;
  private int nb;
  private ArrayList<Integer> listOfRolls;

  Die(int nb, NavigationPane np, ArrayList<Integer> listOfRolls)
  {
    super("sprites/pips" + nb + ".gif", 7);
    this.nb = nb;
    this.np = np;
    this.listOfRolls = listOfRolls;
  }

  public void act()
  {
    showNextSprite();
    if (getIdVisible() == 6)
    {
      setActEnabled(false);
      np.startMoving(stepsToMove());
    }
  }

  private int stepsToMove()  {
    int sum = 0;
    for (Integer listOfRoll : this.listOfRolls) {
      sum += listOfRoll;
    }
    return sum;
  }

}
