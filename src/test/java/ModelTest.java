
import java.io.IOException;
import java.util.Random;
import org.apache.log4j.Logger;
import org.graphwalker.Util;
import org.graphwalker.generators.PathGenerator;

public class ModelAPI extends org.graphwalker.multipleModels.ModelAPI {
  private static Logger logger = Util.setupLogger(ModelAPI.class);
  private static final Random random = new Random();
  private static final char[] SPEGS = {'r', 'o', 'y', 'g', 'b', 'i'};
  private static final char[] CPEGS = {'r', 'o', 'y', 'g', 'b', 'i', 'v'};
  private BlackBox bb;

  public ModelAPI(File model, boolean efsm, PathGenerator generator, boolean weight) {
    super(model, efsm, generator, weight);
  }


  /**
   * This method implements the Edge 'e_SelectChallenge'
   * 
   */
  public void e_SelectChallenge() throws ProgramTerminatedException {
      bb.performInput('c');
  }


  /**
   * This method implements the Edge 'e_SelectContinue'
   * 
   */
  public void e_SelectContinue() throws ProgramTerminatedException {
      bb.performInput('c');
  }


  /**
   * This method implements the Edge 'e_SelectHint'
   * 
   */
  public void e_SelectHint() throws ProgramTerminatedException {
      bb.performInput('h');
  }


  /**
   * This method implements the Edge 'e_SelectNo'
   * 
   */
  public void e_SelectNo() throws ProgramTerminatedException {
      bb.performInput('n');
  }


  /**
   * This method implements the Edge 'e_SelectPeg'
   * 
   */
  public void e_SelectPeg() throws ProgramTerminatedException {
      char input = '';
      Integer mode = Integer.valueOf(getMbt().getDataValue("n"));
      if (mode == 4)
          input = SPEGS[random.nextInt(SPEGS.length)];
      else if (mode == 5)
          input = CPEGS[random.nextInt(CPEGS.length)];
      bb.performInput(input);
  }


  /**
   * This method implements the Edge 'e_SelectStandard'
   * 
   */
  public void e_SelectStandard() throws ProgramTerminatedException {
      bb.performInput('s');
  }


  /**
   * This method implements the Edge 'e_SelectStartNew'
   * 
   */
  public void e_SelectStartNew() throws ProgramTerminatedException {
      bb.performInput('s');
  }


  /**
   * This method implements the Edge 'e_SelectStats'
   * 
   */
  public void e_SelectStats() throws ProgramTerminatedException {
      bb.performInput('s');
  }


  /**
   * This method implements the Edge 'e_SelectYes'
   * 
   */
  public void e_SelectYes() throws ProgramTerminatedException {
      bb.performInput('y');
  }


  /**
   * This method implements the Edge 'e_StartProgram'
   * 
   */
  public void e_StartProgram() throws IOException {
      String[] args = {""};
      bb.run(args);
  }


  /**
   * This method implements the Vertex 'v_FinalPeg'
   * 
   */
  public void v_FinalPeg() throws ProgramTerminatedException {
      String[] output = bb.readAndPrintLines();
      if (output.length > 0 && output[4].startsWith("Would you like to continue or start a new game or get a hint?"))
        setCurrentVertex("v_GuessResult");
      else if (output.length > 0 && output[5].startsWith("It took you "))
        setCurrentVertex("v_MainMenu");
        //TODO: test number of guesses
      else
          bb.test("Test v_FinalPeg", false);
  }


  /**
   * This method implements the Vertex 'v_GuessResult'
   * 
   */
  public void v_GuessResult() throws ProgramTerminatedException {
    //What should we test here?
  }


  /**
   * This method implements the Vertex 'v_MainMenu'
   * 
   */
  public void v_MainMenu() throws ProgramTerminatedException {
      //Problem: what if set to Main Menu through win game?
      String[] output = bb.readAndPrintLines();
      bb.test("Test v_MainMenu", output.length > 0 && output[0].startsWith("Ready to start a new game?"));

      //OR (if chosen show statistics)
      //String[] output = bb.readAndPrintLines();
      //bb.test("Test v_MainMenu", output.length > 0 && output[1].startsWith("MasterMind Statistics"));
  }


  /**
   * This method implements the Vertex 'v_ModeMenu'
   * 
   */
  public void v_ModeMenu() throws ProgramTerminatedException {
      String[] output = bb.readAndPrintLines();
      bb.test("Test v_ModeMenu", output.length > 0 && output[0].startsWith("Please choose: "));
  }


  /**
   * This method implements the Vertex 'v_RequestPeg'
   * 
   */
  public void v_RequestPeg() throws ProgramTerminatedException {
      //How to check first peg?
      String[] output = bb.readAndPrintLines();
      if (Integer.valueOf(getMbt().getDataValue("n")) > 1)
          bb.test("Test v_ModeMenu", output.length > 0 && output[0].startsWith("Color of peg "));
  }


  /**
   * This method implements the Vertex 'v_Stopped'
   * 
   */
  public void v_Stopped() throws ProgramTerminatedException {
      String[] output = bb.readAndPrintLines();
      bb.test("Test v_Stopped", output.length > 0 && output[0].startsWith("Thank you for playing! Bye!"));
  }


}

