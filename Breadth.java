//Import packages
//import java.io.BufferedReader;
import java.io.InputStreamReader;
import lejos.nxt.*;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.*;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;

public class Breadth 
{

	/**
	 * Robot travels from A to letter chosen 
	 * Calculates path using breadth first search
	 * 
	 * author: Becca Tinkler
	 * 
	 */
	
	// Make coord tree
    static class Node 
    {
        String name;
        int x, y;
        Node left, right;

        Node(String name, int x, int y) 
        {
            this.name = name;
            this.x = x;
            this.y = y;
        }
    }

    // Set up needed variables
    Node root;
    Node[] path;
    int pathL;

    DifferentialPilot pilot;
    NavPathController nav;

    // Coord tree
    // Increase values given on same scale so
    // its easier to see
    void coordTree() {
    	Node A = new Node("A", 0, 0);      
        Node B = new Node("B", 20, 20);  
        Node C = new Node("C", -20, 20);  
        Node D = new Node("D", 40, 40);  
        Node E = new Node("E", 0, 40);    
        Node F = new Node("F", 20, 60);  
        Node G = new Node("G", -20, 60);  
        Node H = new Node("H", 0, 80);    
        Node I = new Node("I", -40, 80);  
        Node J = new Node("J", 20, 100);  
        Node K = new Node("K", -20, 100);  
        Node L = new Node("L", 40, 120);  
        Node M = new Node("M", 0, 120); 

        // Make branches
        root = A;
        A.left = B;
        A.right = C;
        B.left = D;
        B.right = E;
        E.left = F;
        E.right = G;
        G.left = H;
        G.right = I;
        H.left = J;
        H.right = K;
        J.left = L;
        J.right = M;
    }
    
 // Use breadth first search for path
    boolean findPath(Node start, String target) 
    {
        Node[] queue = new Node[50];
        Node[] parent = new Node[50];
        int front = 0, rear = 0;

        // enqueue start
        queue[rear] = start;
        parent[rear] = null;
        rear++;

        Node targetNode = null;

        while (front < rear) 
        {
            Node current = queue[front];
            Node currentParent = parent[front];
            front++;

            // Check if target found
            if (current.name.equals(target)) 
            {
                targetNode = current;
                break;
            }

            // Add left child
            if (current.left != null) 
            {
                queue[rear] = current.left;
                parent[rear] = current;
                rear++;
            }

            // Add right child
            if (current.right != null) 
            {
                queue[rear] = current.right;
                parent[rear] = current;
                rear++;
            }
        }

        // If not found
        if (targetNode == null) return false;

        // reconstruct path (backwards)
        Node[] reversePath = new Node[50];
        int count = 0;

        Node step = targetNode;

        while (step != null) 
        {
            reversePath[count++] = step;

            // Find parent of this step
            for (int i = 0; i < rear; i++) 
            {
                if (queue[i] == step) 
                {
                    step = parent[i];
                    break;
                }
            }
        }

        // Reverse into correct order
        pathL = count;
        for (int i = 0; i < count; i++) 
        {
            path[i] = reversePath[count - i - 1];
        }

        return true;
    }
    
	public static void main(String[] args) 
	{
		// Display program name
    	LCD.drawString("BFS", 0, 0);
    	LCD.drawString("Algorithm", 0, 1);
    						
    	// Wait for button press to start
    	Button.waitForPress();
    	LCD.clear();

        Breadth ob = new Breadth();
        //BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        // Make tree
        ob.coordTree();
        ob.path = new Node[20];
        
        // Find path from A (root) to user input
        //System.out.print("To? ");
        //String to = br.readLine();
        boolean found = ob.findPath(ob.root, "M");

        // If target node cannot be found,
        // display message to user and wait
        // for enter to be pressed to end
        if (!found) 
        {
            LCD.clear();
            LCD.drawString("Coordinate not", 0, 0);
            LCD.drawString("found", 0, 1);
            Button.ENTER.waitForPressAndRelease();
            return;
        }

        // Set up differential pilot + navpath
        double diam = 5.6;
		double trackwidth = 16;
		ob.pilot = new DifferentialPilot(diam, trackwidth, Motor.A, Motor.C); 
		ob.nav = new NavPathController(ob.pilot);
        
        // Move on found path
        for (int i = 0; i < ob.pathL; i++) {
            Node n = ob.path[i];

            //Display next target and wait for user to press enter to continue
            LCD.clear();
            LCD.drawString("Next Coord: " + n.name, 0, 0);
            LCD.drawString("X: " + n.x + " Y: " + n.y, 0, 1);
            LCD.drawString("Press ENTER", 0, 3);
            Button.ENTER.waitForPressAndRelease();

            WayPoint wp = new WayPoint(n.x, n.y);
            ob.nav.goTo(wp);
            while(ob.nav.isGoing()) {
                Thread.yield();  // small pause to let CPU breathe
            }
        }
		
        // Display arrived message
        // Wait for button press to end
        LCD.clear();
        LCD.drawString("Arrived", 0, 0);
        LCD.drawString("Press ENTER to", 0, 1);
        LCD.drawString("exit", 0, 2);
        Button.ENTER.waitForPressAndRelease();
	}
}