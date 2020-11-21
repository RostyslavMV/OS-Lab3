// Run() is called from Scheduling.main() and is where
// the scheduling algorithm written by the user resides.
// User modification should occur within the Run() function.

import java.util.*;
import java.io.*;

public class SchedulingAlgorithm {

    public static Results Run(int runtime, Vector processVector, Results result, int usernum, int maxTimeForUser) {
        int comptime = 0;
        int time = 0;
        int currentUser = 0;
        int previousUser;
        int currentUserRunningTime = 0;
        boolean isRunning;
        int size = processVector.size();
        int completed = 0;
        String resultsFile = "Summary-Processes";
        result.schedulingType = "Interactive (Preemptive)";
        result.schedulingName = "Fair-share";
        result.compuTime = 0;
        List<Queue<sProcess>> usersQueue = new ArrayList<>();

        for (int id = 0; id < usernum; ++id) {
            usersQueue.add(new LinkedList<>());
        }
        for (int i = 0; i < processVector.size(); ++i) {
            sProcess curProcess = (sProcess) processVector.elementAt(i);
            usersQueue.get(curProcess.userId).add(curProcess);
        }

        processVector.clear();

        try {
            //BufferedWriter out = new BufferedWriter(new FileWriter(resultsFile));
            //OutputStream out = new FileOutputStream(resultsFile);
            PrintStream out = new PrintStream(new FileOutputStream(resultsFile));
            while (currentUser < usernum && usersQueue.get(currentUser).isEmpty())
                currentUser++;
            if (currentUser == usernum) {
                out.close();
                return result;
            }
            out.println("Time: " + comptime);
            out.println("User Id: " + currentUser);
            sProcess process = usersQueue.get(currentUser).remove();
            out.println("Process: " + process.id + ", user Id: " + process.userId + " registered... (" + process.cputime
                    + " " + process.ioblocking + " " + process.cpudone + ")");
            isRunning = true;
            while (comptime < runtime) {
                if (process.cpudone == process.cputime) {
                    processVector.add(process);
                    completed++;
                    out.println("Process: " + process.id + ", user Id: " + process.userId + " completed... (" + process.cputime
                            + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
                    if (completed == size) {
                        result.compuTime = comptime;
                        out.close();
                        return result;
                    }

                    if (usersQueue.get(currentUser).isEmpty() || currentUserRunningTime == maxTimeForUser) {
                        comptime += maxTimeForUser - currentUserRunningTime;
                        if (comptime >= runtime)
                            break;
                        previousUser = currentUser;
                        currentUserRunningTime = 0;
                        currentUser = (currentUser + 1) % usernum;
                        while (usersQueue.get(currentUser).isEmpty())
                            currentUser = (currentUser + 1) % usernum;
                        if (previousUser != currentUser) {
                            out.println("\nTime " + comptime);
                            out.println("Change user " + previousUser + " to " + currentUser);
                        }
                    }

                    process = getNextProcess(usersQueue, currentUser, comptime);
                    if (process.getTimeLeftToUnblock(comptime) > 0) {
                        isRunning = false;
                        out.println("Waiting for process unblocking... User Id: " + process.userId + " Process Id: " + process.id);
                    } else {
                        isRunning = true;
                        out.println("Process: " + process.id + ", user Id: " + process.userId + " registered... ("
                                + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
                    }
                }

                if (process.ioblocking == process.ionext) {
                    out.println("Process: " + process.id + ", user Id: " + process.userId + " I/O blocked... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
                    process.numblocked++;
                    process.ionext = 0;
                    process.blockingTime = comptime;

                    if (usersQueue.get(currentUser).isEmpty()) {
                        isRunning = false;
                        out.println("Waiting for process unblocking... User Id: " + process.userId + " Process Id: " + process.id);
                    } else {
                        usersQueue.get(currentUser).add(process);

                        process = getNextProcess(usersQueue, currentUser, comptime);
                        if (process.getTimeLeftToUnblock(comptime) > 0) {
                            isRunning = false;
                            out.println("Waiting for process unblocking... User Id: " + process.userId + " Process " +
                                    "Id: " + process.id);
                        } else {
                            isRunning = true;
                            out.println("Process: " + process.id + ", user Id: " + process.userId + " registered... " +
                                    "(" + process.cputime + " " + process.ioblocking + " " + process.cpudone + ")");
                        }
                    }
                }

                if (currentUserRunningTime >= maxTimeForUser) {
                    previousUser = process.userId;
                    usersQueue.get(currentUser).add(process);

                    currentUserRunningTime = 0;
                    currentUser = (currentUser + 1) % usernum;
                    while (usersQueue.get(currentUser).isEmpty())
                        currentUser = (currentUser + 1) % usernum;

                    if (previousUser != currentUser) {
                        if (isRunning)
                            out.println("Process: " + process.id + ", user Id: " + process.userId + " blocked by " +
                                    "changing user... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + ")");

                        out.println("\nTime " + comptime);
                        out.println("Change user " + previousUser + " to " + currentUser);
                    }

                    process = getNextProcess(usersQueue, currentUser, comptime);
                    if (process.getTimeLeftToUnblock(comptime) > 0) {
                        isRunning = false;
                        out.println("Waiting for process unblocking... User Id: " + process.userId + " Process Id: " + process.id);
                    } else {
                        isRunning = true;
                        out.println("Process: " + process.id + ", user Id: " + process.userId + " registered... ("
                                + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
                    }
                }

                if (!isRunning) {
                    int blockingTimeLeft = process.getTimeLeftToUnblock(comptime);
                    if (blockingTimeLeft == 0) {
                        isRunning = true;
                        out.println("Process: " + process.id + ", user Id: " + process.userId + " registered... ("
                                + process.cputime + " " + process.ioblocking + " " + process.cpudone + ")");
                    }
                } else {
                    process.cpudone++;
                    if (process.ioblocking > 0) {
                        process.ionext++;
                    }
                }
                currentUserRunningTime++;
                comptime++;
            }
            processVector.add(process);
            for (int i = 0; i < usernum; ++i) {
                while (!usersQueue.get(i).isEmpty()) {
                    sProcess curProcess = usersQueue.get(i).remove();
                    processVector.add(curProcess);
                }
            }
            out.println("Runtime ended. Scheduler working time: " + comptime);
            out.close();
        } catch (IOException e) { /* Handle exceptions  */}
        result.compuTime = comptime;
        return result;
    }

    private static sProcess getNextProcess(List<Queue<sProcess>> usersQueue, int currentUser, int comptime) {
        sProcess nextProcess = usersQueue.get(currentUser).remove();
        int minBlockingTimeLeft = nextProcess.getTimeLeftToUnblock(comptime);
        int n = usersQueue.get(currentUser).size();
        for (int i = 0; i < n && minBlockingTimeLeft > 0; ++i) {
            sProcess curProcess = usersQueue.get(currentUser).remove();
            int curBlockingTimeLeft = curProcess.getTimeLeftToUnblock(comptime);
            if (curBlockingTimeLeft < minBlockingTimeLeft) {
                usersQueue.get(currentUser).add(nextProcess);
                nextProcess = curProcess;
                minBlockingTimeLeft = curBlockingTimeLeft;
            } else {
                usersQueue.get(currentUser).add(curProcess);
            }
        }
        return nextProcess;
    }
}