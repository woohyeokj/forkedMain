package snakeladder.game;

import ch.aplu.jgamegrid.*;
import java.awt.Point;
import java.util.ArrayList;

public class Puppet extends Actor
{
  private GamePane gamePane;
  private NavigationPane navigationPane;
  private int cellIndex = 0;
  private int nbSteps;
  private Connection currentCon = null;
  private int y;
  private int dy;
  private boolean isAuto;
  private String puppetName;
  private int initialPip;
  private int pipsArray[];
  private int puppetInter[];
  private boolean wasDisplaced = false;

  Puppet(GamePane gp, NavigationPane np, String puppetImage)
  {
    super(puppetImage);
    this.gamePane = gp;
    this.navigationPane = np;
    createPuppetArray();
  }

  public boolean isAuto() {
    return isAuto;
  }

  public void setAuto(boolean auto) {
    isAuto = auto;
  }

  public String getPuppetName() {
    return puppetName;
  }

  public void setPuppetName(String puppetName) {
    this.puppetName = puppetName;
  }

  void go(int nbSteps)
  {
    if (cellIndex == 100)  // after game over
    {
      cellIndex = 0;
      setLocation(gamePane.startLocation);
    }
    this.nbSteps = nbSteps;
    if(!wasDisplaced){
      initialPip = nbSteps;
      savePipToArray(initialPip);
    }
    setActEnabled(true);
    if(!wasDisplaced) autoToggle();
  }

  void resetToStartingPoint() {
    cellIndex = 0;
    setLocation(gamePane.startLocation);
    setActEnabled(true);
  }

  int getCellIndex() {
    return cellIndex;
  }

  private void moveToNextCell()
  {
    int tens = cellIndex / 10;
    int ones = cellIndex - tens * 10;
    if (tens % 2 == 0)     // Cells starting left 01, 21, .. 81
    {
      if (ones == 0 && cellIndex > 0)
        setLocation(new Location(getX(), getY() - 1));
      else
        setLocation(new Location(getX() + 1, getY()));
    }
    else     // Cells starting left 20, 40, .. 100
    {
      if (ones == 0)
        setLocation(new Location(getX(), getY() - 1));
      else
        setLocation(new Location(getX() - 1, getY()));
    }
    cellIndex++;
  }

  private void moveToPreviousCell()
  {
    int tens = cellIndex / 10;
    int ones = cellIndex - tens * 10;
    if (tens % 2 == 0)     // Cells starting left 01, 21, .. 81
    {
      if (ones == 0 && cellIndex > 0)
        setLocation(new Location(getX(), getY() + 1));
      else
        setLocation(new Location(getX() - 1, getY()));

    }
    else     // Cells starting left 20, 40, .. 100
    {
      if (ones == 0)
        setLocation(new Location(getX(), getY() + 1));
      else
        setLocation(new Location(getX() + 1, getY()));

    }
    cellIndex--;
  }

  public void act()
  {
    if ((cellIndex / 10) % 2 == 0)
    {
      if (isHorzMirror())
        setHorzMirror(false);
    }
    else
    {
      if (!isHorzMirror())
        setHorzMirror(true);
    }

    // Animation: Move on connection
    if (currentCon != null)
    {
      int x = gamePane.x(y, currentCon);
      setPixelLocation(new Point(x, y));
      y += dy;
      // Check end of connection
      actCondition();
      return;
    }

    // Normal movement
    if (nbSteps > 0)
    {
      moveToNextCell();

      if (cellIndex == 100)  // Game over
      {
        setActEnabled(false);
        navigationPane.prepareRoll(cellIndex);
        return;
      }
      nbSteps--;
      if (nbSteps == 0)
      {
        if (sameSquare() != -1) {
          gamePane.getAllPuppets().get(sameSquare()).displace();
        }
        checkCon();
      }
    }

    // Movement on encounter
    if (nbSteps < 0)
    {
      moveToPreviousCell();
      nbSteps++;
      if (nbSteps == 0)
      {
        checkCon();
      }
    }
  }

  public int sameSquare() {
    ArrayList<Puppet> puppets = (ArrayList<Puppet>) this.gamePane.getAllPuppets();
    for (int i = 0; i < gamePane.getNumberOfPlayers(); i++) {
      if (!this.puppetName.equals(puppets.get(i).puppetName)) {
        if (this.cellIndex == puppets.get(i).cellIndex) {
          this.gamePane.getAllPuppets().get(i).toDisplace();
          return i;
        }
      }
    }
    return -1;
  }


  public void checkCon(){
    // Check if on connection start
    if ((currentCon = gamePane.getConnectionAt(getLocation())) != null)
    {
      actionSub();
      if (currentCon instanceof Snake)
      {
        navigationPane.showStatus("Digesting...");
        navigationPane.playSound(GGSound.MMM);
      }
      else
      {
        navigationPane.showStatus("Climbing...");
        navigationPane.playSound(GGSound.BOING);
      }
    }
    else
    {
      setActEnabled(false);
      if(!wasDisplaced) navigationPane.prepareRoll(cellIndex);
    }
  }

  public void toDisplace() {
    wasDisplaced = true;
  }

  public void displace() {
    go(-1);
    wasDisplaced = false;
    navigationPane.prepareRoll(cellIndex);
  }

  //initialise arrays puppetInter[] and pipsArray[] to keep statistics
  public void createPuppetArray(){
    puppetInter = new int[2];
    pipsArray = new int[navigationPane.getNumberOfDice()*6+1];
  }

  //save the input of the pip into the pipArray[]
  public void savePipToArray(int nb){
    int minVal = navigationPane.getNumberOfDice();
    for(int i = minVal; i<minVal*6+1; i++){
      if(i == nb){ pipsArray[i]+=1; }
    }
  }

  //function to create output of the player's pip history
  public void showStats(){
    int minVal = navigationPane.getNumberOfDice();
    System.out.printf("%s rolled: ", puppetName);
    for(int i = minVal; i<minVal*6+1; i++){
      if(i == minVal*6)
        System.out.printf("%d-%d\n", i, pipsArray[i]);
      else
        System.out.printf("%d-%d, ", i, pipsArray[i]);
    }
  }

  //function to create output of the player's interaction history
  public void showInter(){
    System.out.printf("%s traversed: up-%d, down-%d\n", puppetName, puppetInter[0], puppetInter[1]);
  }

  public void actCondition(){
    if(!navigationPane.toggleStat()){
      if ((dy > 0 && (y - gamePane.toPoint(currentCon.locEnd).y) > 0)
              || (dy < 0 && (y - gamePane.toPoint(currentCon.locEnd).y) < 0))
      {
        gamePane.setSimulationPeriod(100);
        setActEnabled(false);
        setLocation(currentCon.locEnd);
        cellIndex = currentCon.cellEnd;
        setLocationOffset(new Point(0, 0));
        currentCon = null;
        navigationPane.prepareRoll(cellIndex);
      }
    }
    //when the toggle button is on
    if(navigationPane.toggleStat()){
      if ((dy > 0 && (y - gamePane.toPoint(currentCon.locStart).y) > 0)
              || (dy < 0 && (y - gamePane.toPoint(currentCon.locStart).y) < 0))
      {
        gamePane.setSimulationPeriod(100);
        setActEnabled(false);
        setLocation(currentCon.locStart);
        cellIndex = currentCon.cellStart;
        setLocationOffset(new Point(0, 0));
        currentCon = null;
        navigationPane.prepareRoll(cellIndex);
      }
    }
  }

  public void actionSub(){
    if(!navigationPane.toggleStat()){
      gamePane.setSimulationPeriod(50);
      y = gamePane.toPoint(currentCon.locStart).y;
      //for the condition going "down" and pip is the lowest possible value ignore the condition
      if(initialPip == navigationPane.getNumberOfDice() && currentCon.locEnd.y > currentCon.locStart.y){
        y = 0;
        currentCon = null;
        setActEnabled(false);
        navigationPane.prepareRoll(cellIndex);
      }
      else {
        if (currentCon.locEnd.y > currentCon.locStart.y){
          dy = gamePane.animationStep;
          puppetInter[1]+=1;
        }
        else{
          dy = -gamePane.animationStep;
          puppetInter[0]+=1;
        }
      }
    }

    //when the toggle button is on
    if(navigationPane.toggleStat()){
      gamePane.setSimulationPeriod(50);
      y = gamePane.toPoint(currentCon.locEnd).y;
      //for the condition going "down" and pip is the lowest possible value ignore the condition
      if(initialPip == navigationPane.getNumberOfDice() && currentCon.locEnd.y < currentCon.locStart.y){
        y = 0;
        currentCon = null;
        setActEnabled(false);
        navigationPane.prepareRoll(cellIndex);
      }
      else {
        if (currentCon.locEnd.y < currentCon.locStart.y){
          dy = gamePane.animationStep;
          puppetInter[1]+=1;
        }
        else{
          dy = -gamePane.animationStep;
          puppetInter[0]+=1;
        }
      }
    }
  }

  public void autoToggle(){
    if(isAuto){
      int changeNeed = 0;
      for(Puppet newPuppet: gamePane.getAllPuppets()){
        int up =0 , down = 0;
        if(newPuppet.puppetName != puppetName){
          for(int i = navigationPane.getNumberOfDice(); i < navigationPane.getNumberOfDice()*6+1; i++){
            //when the toggle is not on
            if(!navigationPane.toggleStat()){
              for(Connection con: gamePane.getConnections()){
                if(con.cellStart == newPuppet.cellIndex+i){
                  if(con.cellStart < con.cellEnd) up++;
                  else down++;
                }
              }
            }

            //when the toggle is on
            if(navigationPane.toggleStat()){
              for(Connection con: gamePane.getConnections()){
                if(con.cellEnd == newPuppet.cellIndex+i){
                  if(con.cellEnd < con.cellStart) up++;
                  else down++;
                }
              }
            }
          }
          if(up >= down) changeNeed++;
        }
      }

      if(changeNeed > (gamePane.getNumberOfPlayers()-1)/2 && !navigationPane.toggleStat())
        navigationPane.toggleCheck();
      else
        navigationPane.toggleUncheck();
    }
  }
}
