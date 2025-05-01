package be.kuleuven;

import java.util.List;

import org.jdbi.v3.core.Jdbi;

public class SpelerRepositoryJDBIimpl implements SpelerRepository {
  private final Jdbi jdbi;

  // Constructor
  SpelerRepositoryJDBIimpl(String connectionString, String user, String pwd) {
    this.jdbi = Jdbi.create(connectionString, user, pwd);
  }

  @Override
  public void addSpelerToDb(Speler speler) {
    jdbi.withHandle(handle -> {
      return handle.createUpdate("INSERT INTO speler (tennisvlaanderenid, naam, punten) VALUES (:tennisvlaanderenid, :naam, :punten)")
          .bindBean(speler)
          .execute();
    });
  }

  @Override
  public Speler getSpelerByTennisvlaanderenId(int tennisvlaanderenId) {
    return jdbi.withHandle(handle -> {
      return handle.createQuery("SELECT * FROM speler WHERE tennisvlaanderenid = :id")
          .bind("id", tennisvlaanderenId)
          .mapToBean(Speler.class)
          .findOne()
          .orElseThrow(() -> new InvalidSpelerException(tennisvlaanderenId + ""));
    });
  }

  @Override
  public List<Speler> getAllSpelers() {
    return jdbi.withHandle(handle -> {
      return handle.createQuery("SELECT * FROM speler")
          .mapToBean(Speler.class)
          .list();
    });
  }

  @Override
  public void updateSpelerInDb(Speler speler) {
    int changedRows = jdbi.withHandle(handle -> {
      return handle.createUpdate("UPDATE speler SET naam = :naam, punten = :punten WHERE tennisvlaanderenid = :tennisvlaanderenid")
          .bindBean(speler)
          .execute();
    });
    if (changedRows == 0) {
      throw new InvalidSpelerException(speler.getTennisvlaanderenid() + "");
    }
  }

  @Override
  public void deleteSpelerInDb(int tennisvlaanderenid) {
    int deletedRows = jdbi.withHandle(handle -> {
      return handle.createUpdate("DELETE FROM speler WHERE tennisvlaanderenid = :id")
          .bind("id", tennisvlaanderenid)
          .execute();
    });
    if (deletedRows == 0) {
      throw new InvalidSpelerException(tennisvlaanderenid + "");
    }
  }

  @Override
  public String getHoogsteRankingVanSpeler(int tennisvlaanderenid) {
    try {
        getSpelerByTennisvlaanderenId(tennisvlaanderenid);
    } catch (Exception e) {
      throw new InvalidSpelerException(tennisvlaanderenid + "");
    }

    return jdbi.withHandle(handle -> {
      return handle.createQuery(
           "SELECT t.clubnaam, w.finale, w.winnaar " +
           "FROM wedstrijd w " +
           "JOIN tornooi t ON w.tornooi = t.id " +
           "WHERE (w.speler1 = :id OR w.speler2 = :id) " +
           "ORDER BY w.finale ASC " +
           "LIMIT 1")
          .bind("id", tennisvlaanderenid)
          .map((rs, ctx) -> {
            String clubnaam = rs.getString("clubnaam");
            int finale = rs.getInt("finale");
            Integer winnaar = rs.getObject("winnaar") != null ? rs.getInt("winnaar") : null;
            
            String finaleString;
            if (finale == 1 && winnaar != null && winnaar == tennisvlaanderenid) {
               finaleString = "winst";
             } else if (finale == 1) {
               finaleString = "finale";
             } else if (finale == 2) {
               finaleString = "halve-finale";
             } else if (finale == 4) {
               finaleString = "kwart-finale";
             } else {
               finaleString = "lager dan de kwart-finales";
             }

             return "Hoogst geplaatst in het tornooi van " + clubnaam + " met plaats in de " + finaleString;
          })
          .findOne()
          .orElse("Geen resultaten gevonden");
    });
  }

  @Override
  public void addSpelerToTornooi(int tornooiId, int tennisvlaanderenId) {
    int addedRows = jdbi.withHandle(handle -> {
      return handle.createUpdate("INSERT INTO speler_speelt_tornooi (speler, tornooi) VALUES (:speler, :tornooi)")
          .bind("speler", tennisvlaanderenId)
          .bind("tornooi", tornooiId)
          .execute();
    });
    if (addedRows == 0) {
      throw new RuntimeException();
    }
  }

  @Override
  public void removeSpelerFromTornooi(int tornooiId, int tennisvlaanderenId) {
    int removedRows = jdbi.withHandle(handle -> {
      return handle.createUpdate("DELETE FROM speler_speelt_tornooi WHERE speler = :speler AND tornooi = :tornooi")
          .bind("speler", tennisvlaanderenId)
          .bind("tornooi", tornooiId)
          .execute();
    });
    if (removedRows == 0) {
      throw new RuntimeException();
    }
  }
}
