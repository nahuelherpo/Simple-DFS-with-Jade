import jade.core.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.Runtime;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class AgenteMovil extends Agent
{

	private List<String> containers = new ArrayList<String>();
	private int position = 0;
	long[][] matrixOfResults = new long[4][2];
	long startTime = 0L;
	private boolean debug = false;

	// Ejecutado por unica vez en la creacion
	public void setup() {

		startTime = System.currentTimeMillis();

		Location origen = here();

		if (debug) {
			System.out.println("\nSETUP\n");
			System.out.println("\nHola, soy el agente migrado mi nombre local es " + getLocalName());
			System.out.println("Mi nombre completo... " + getName());
			System.out.println("Estoy en la location " + origen.getID() + "\n");
		}

		for (int i = 1; i <= 4; i++ )
			containers.add("Container-" + i);

		containers.add("Main-Container");

		// Para migrar el agente
		try {
			//ContainerID destino = new ContainerID("Main-Container", null);
			ContainerID destino = new ContainerID(containers.get(position), null);
			position++;
			System.out.println("Migrando el agente a " + destino.getID());
			doMove(destino);
		} catch (Exception e) {
			System.out.println("No fue posible migrar el agente\n");
		}

	}

	// Ejecutado al llegar a un contenedor como resultado de una migracin
	protected void afterMove() {

		Location origen = here();

		if (debug) {
			System.out.println("\nAFTER_MOVE\n");
			System.out.println("\nHola, soy el agente migrado mi nombre local es " + getLocalName());
			System.out.println("Mi nombre completo... " + getName());
			System.out.println("Estoy en la location " + origen.getID() + "\n");
		}

		if (!origen.getName().equals("Main-Container")) {

			Runtime runtime = Runtime.getRuntime();
			ThreadMXBean thread = ManagementFactory.getThreadMXBean();
	
			if (debug) {
				System.out.println("Free memory (JVM) " + runtime.freeMemory() + " bytes");
				System.out.println("Current thread CPU time " + thread.getCurrentThreadCpuTime() + " nanoseconds");
			}

			int containerNumber = Integer.valueOf(origen.getID().split("-")[1].split("@")[0]);
		
			matrixOfResults[containerNumber - 1][0] = runtime.freeMemory();
			matrixOfResults[containerNumber - 1][1] = thread.getCurrentThreadCpuTime();	

			ContainerID destino;
			try {
				//ContainerID destino = new ContainerID("Main-Container", null);
				destino = new ContainerID(containers.get(position), null);
				position++;
				System.out.println("Migrando el agente a " + destino.getID());
				doMove(destino);
			} catch (Exception e) {
				System.out.println("No fue posible migrar el agente\n");
			}
		} else {

			if (debug)
				System.out.println("Efectivamente estas en el main\n Mostrando todos los datos recopilados en los contenedores\n");
			
			long endTime = System.currentTimeMillis();
		
			System.out.println("Time of round " + (endTime - startTime) + " milliseconds");

			System.out.println("Container#		Free memory (bytes)	CPU time (ns)");
			for (int i=0; i<4; i++) {
				System.out.print("Container" + i + "  		");
				for (int j=0; j<2; j++)
					System.out.print(matrixOfResults[i][j] + "  		");
				System.out.println();
			}
			
		}

	}

}
