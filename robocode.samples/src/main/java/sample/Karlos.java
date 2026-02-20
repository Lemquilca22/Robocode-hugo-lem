package sample;

import robocode.*;
import robocode.util.Utils;
import java.awt.Color;

public class Karlos extends AdvancedRobot {

    String nombreObjetivo = "";
    double vidaMinima = Double.MAX_VALUE;
    // Añadimos una variable para controlar la dirección (1 adelante, -1 atrás)
    int direccion = 1;

    public void run() {
        setBodyColor(Color.pink);
        setGunColor(Color.black);
        setRadarColor(Color.black);
        setBulletColor(Color.white);

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while (true) {
            // MEJORA 1: Movimiento más fluido
            // Si el robot ya casi terminó de moverse, le damos una nueva orden
            if (getDistanceRemaining() == 0) {
                setTurnRight(10000);
                setAhead(10000 * direccion);
            }

            // Gestión de velocidad por energía
            if (getEnergy() < 20) {
                setMaxVelocity(8);
                setBodyColor(Color.red);
            } else {
                setMaxVelocity(5);
                setBodyColor(Color.pink);
            }

            if (getRadarTurnRemaining() == 0) {
                setTurnRadarRight(360);
            }

            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        // Lógica de selección del más débil
        if (nombreObjetivo.equals("") || e.getEnergy() < vidaMinima || e.getName().equals(nombreObjetivo)) {
            nombreObjetivo = e.getName();
            vidaMinima = e.getEnergy();

            double anguloAbsoluto = getHeading() + e.getBearing();
            double giroCanon = Utils.normalRelativeAngleDegrees(anguloAbsoluto - getGunHeading());
            setTurnGunRight(giroCanon);

            if (Math.abs(giroCanon) < 5) {
                setFire(Math.min(400 / e.getDistance(), 3));
            }

            // MEJORA 2: Huida cuerpo a cuerpo corregida
            if (e.getDistance() < 150) {
                direccion *= -1; // Invertimos el sentido de la marcha
                setAhead(200 * direccion);
                setTurnLeft(90);
            }
        }
    }

    // MEJORA 3: Evento onHitRobot (Fundamental para no quedarse trabado)
    public void onHitRobot(HitRobotEvent e) {
        direccion *= -1; // Cambiar dirección al chocar
        setAhead(200 * direccion);
    }

    public void onRobotDeath(RobotDeathEvent e) {
        if (e.getName().equals(nombreObjetivo)) {
            nombreObjetivo = "";
            vidaMinima = Double.MAX_VALUE;
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        setTurnLeft(90 - e.getBearing());
    }
}