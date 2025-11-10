package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.services.DataService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TarjetaEventoRepositoryTest {

  private static TarjetaEventoRepository tarjetaEventoRepository;
  private static final DataService dataService =
      new DataService("jdbc:h2:mem:Pontiland;DB_CLOSE_DELAY=-1");

  @BeforeAll
  public static void setup() {
    dataService.newDataBase();
    tarjetaEventoRepository = new TarjetaEventoRepository(dataService);
  }

  @Test
  public void test() {
    System.out.println(tarjetaEventoRepository.getRandomTarjetaEvento());
  }
}
