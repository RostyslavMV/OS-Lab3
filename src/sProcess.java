public class sProcess {

  public static int blockingDuration = 20;

  public int id;
  public int userId;
  public int cputime;
  public int ioblocking;
  public int cpudone;
  public int ionext;
  public int numblocked;
  public int blockingTime;

  public sProcess (int id, int userId, int cputime, int ioblocking, int cpudone, int ionext, int numblocked) {
    this.id = id;
    this.cputime = cputime;
    this.ioblocking = ioblocking;
    this.cpudone = cpudone;
    this.ionext = ionext;
    this.numblocked = numblocked;
    this.userId = userId;
  }

  public int getTimeLeftToUnblock(int comptime)
  {
    return Math.max(0, blockingDuration - (comptime - blockingTime));
  }
}
