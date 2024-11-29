package ejercicioBingo;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Clase que sirve para crear cada uno de los hilos de los jugadores
 */
class Jugador extends Thread {
	final int TOTAL_CARTON = 5; // Cantidad de números por cartón
	int idJugador; // Identificador del jugador
	Set<Integer> carton; // Números pendientes de acertar
	private boolean bingo = false;
	private final Bombo bombo;

	// Constructor
	Jugador(int idJugador, Bombo bombo) {
		this.idJugador = idJugador;
		this.bombo = bombo;
		carton = new HashSet<>();
		while (carton.size() < TOTAL_CARTON) {
			carton.add((int) Math.floor(Math.random() * bombo.TOTAL_BOMBO) + 1);
		}
	}

	// Mostramos los números del cartón pendientes de acertar
	void imprimeCarton() {
		System.out.print("Cartón jugador " + idJugador + ": ");
		for (Integer num : carton)
			System.out.print(num + " ");
		System.out.println();
	}

	@Override
	public synchronized void run() {
		while (bingo == false) {
			// Esperamos a que haya un nuevo número del bombo
			while (bombo.ultNumero == null || bombo.bombo.contains(bombo.ultNumero)) {
				try {
					wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

			// Tachamos el número si está en el cartón
			carton.remove(bombo.ultNumero);

			// Verificamos si se canta Bingo
			if (carton.isEmpty()) {
				bingo = true;
				System.out.println("¡El jugador " + idJugador + " canta BINGO!");
				bombo.setBingo(true); // Notificamos que el Bingo ha terminado
				bombo.notifyAll();
			} else {
				System.out.println("Jugador " + idJugador + " pendientes: " + carton);
			}
		}
	}
}

/**
 * Clase para el presentador
 */
class Presentador extends Thread {
	private final Bombo bombo;

	// Constructor
	Presentador(Bombo bombo) {
		this.bombo = bombo;
	}

	@Override
	public synchronized void run() {
		while (!bombo.isBingo()) {
			if (!bombo.isBingo()) {
				Integer numero = bombo.sacarNum();
				if (numero == null) {
					System.out.println("No quedan más números en el bombo.");
					System.exit(0);
				} else {
					System.out.println("El presentador saca el número: " + numero);
				}
			}
			// Mostramos los números del bombo, es decir los numeros que han salido.
			bombo.imprimirBombo();
			try {
				// Esperamos 1 segundo para simular que sacamos un numero.
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			// Notificamos a los jugadores
			this.notifyAll();
		}
	}
}

/**
 * Clase que se utiliza para crear el objeto compartido entre los hilos
 */
class Bombo {
	final int TOTAL_BOMBO = 10; // Números posibles en el bombo
	Set<Integer> bombo; // Valores que han salido
	Integer ultNumero; // Último número del bombo
	private boolean bingo = false; // Indicador para controlar si hay bingo o no.

	// Constructor
	Bombo() {
		bombo = new HashSet<>();
	}

	// Sacar un número del bombo
	synchronized Integer sacarNum() {
		if (bombo.size() < TOTAL_BOMBO) {
			do {
				ultNumero = (int) Math.floor(Math.random() * TOTAL_BOMBO) + 1;
			} while (bombo.contains(ultNumero));
			bombo.add(ultNumero);
			return ultNumero;
		}
		return null;
	}

	// Mostramos los números sacados del bombo
	synchronized void imprimirBombo() {
		System.out.print("Bolas sacadas: ");
		for (Integer num : bombo)
			System.out.print(num + " ");
		System.out.println();
	}

	// Getter para el estado de Bingo
	synchronized boolean isBingo() {
		return bingo;
	}

	// Setter para el estado de Bingo
	synchronized void setBingo(boolean bingo) {
		this.bingo = bingo;
	}
}

/**
 * Clase principal
 */
public class Bingo {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		// Preguntamos cuanto jugadores van a jugar al juego del bingo y creamos un
		// array de jugadores.
		System.out.println("Dime cuántos jugadores van a jugar:");
		int numJugadores = Integer.parseInt(sc.nextLine());
		// Creamos el objeto compartido por los jugadores y el presentador(bombo) y
		// tambien creamos el objeto presentador.
		Bombo bombo = new Bombo();
		Presentador presentador = new Presentador(bombo);
		// Creamos los jugadores de la partida.
		Jugador[] jugadores = new Jugador[numJugadores];
		for (int i = 0; i < numJugadores; i++) {
			jugadores[i] = new Jugador(i + 1, bombo);
			// Mostramos el cartón de cada jugador al inicio del juego del Bingo.
			jugadores[i].imprimeCarton();
		}
		// Iniciamos los hilos de los jugadores y el hilo del presentador.
		for (Jugador jugador : jugadores) {
			jugador.start();
		}
		presentador.start();
		// Esperamos a que terminen los hilos.
		for (Jugador jugador : jugadores) {
			try {
				jugador.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// Finalizamos el juego cuando el presentador termine tambien.
		try {
			presentador.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//
		System.out.println("El juego ha terminado.");
		sc.close();
	}
}