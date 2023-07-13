//package repositories;
//
//import models.Examen;
//
//import java.util.*;
//
//public class ExamenRepositoryImpl implements ExamenRepository {
//    @Override
//    public Examen guardar(Examen examen) {
//        return null;
//    }
//
//    @Override
//    public List<Examen> findAll() {
//        return Collections.emptyList();
//
//        // no funciona si la lista se encuentra vacia
//        /* Arrays.asList(
//                new Examen(5L, "Matematicas"),
//                new Examen(6L, "Lenguaje"),
//                new Examen(7L, "Historia")
//        ); */
//    }
//}

// ESTE ARCHIVO SE ELIMINA, YA NO ES NECESARIO GRACIAS A MOCKITO

package repositories;
import models.Examen;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExamenRepositoryImpl implements ExamenRepository {
    @Override
    public Examen guardar(Examen examen) {
        System.out.println("ExamenRepositoryImpl.guardar");
        return Datos.EXAMEN;
    }

    @Override
    public List<Examen> findAll() {
        System.out.println("ExamenRepositoryImpl.findAll");
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Datos.EXAMENES;
    }
}
