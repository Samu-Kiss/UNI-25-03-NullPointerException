package com.NullPtr.Pontiland.repository;

import com.NullPtr.Pontiland.repository.IPartidaRepository;
import com.NullPtr.Pontiland.repository.PartidaRepository;
import com.NullPtr.Pontiland.services.DataService;
import com.NullPtr.Pontiland.services.IDataService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PropiedadRepositoryTest {
  private static IDataService dataService;
  private static IPartidaRepository partidaRepository;
  private static IJugadorRepository jugadorRepository;

  @BeforeAll
  public static void setup() {
    dataService = new DataService("jdbc:h2:mem:Pontiland;DB_CLOSE_DELAY=-1");
    partidaRepository = new PartidaRepository(dataService);
  }

  @BeforeEach
  public void beforeEach() {
    dataService.deleteDataBase();
    dataService.newDataBase();
  }

  @Test
  public void testGetPropiedadByPositionNoOwner() {

  }

}
