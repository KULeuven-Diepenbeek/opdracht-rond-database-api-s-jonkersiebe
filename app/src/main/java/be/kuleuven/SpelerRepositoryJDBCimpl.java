package be.kuleuven;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SpelerRepositoryJDBCimpl implements SpelerRepository {
  private Connection connection;

  // Constructor
  SpelerRepositoryJDBCimpl(Connection connection) {
    this.connection = connection;
  }

  @Override
  public void addSpelerToDb(Speler speler) {
    try {
        PreparedStatement prepared = (PreparedStatement) connection.prepareStatement("INSERT INTO speler (tennisvlaanderenid, naam, punten) VALUES (?, ?, ?);");
        prepared.setInt(1, speler.getTennisvlaanderenid());
        prepared.setString(2, speler.getNaam());
        prepared.setInt(3, speler.getPunten());
        prepared.executeUpdate();
        prepared.close();
        connection.commit();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Speler getSpelerByTennisvlaanderenId(int tennisvlaanderenId) {
    Speler gevonden_speler = null;
    try {
        Statement s = (Statement) connection.createStatement();
        String query = "SELECT * FROM speler WHERE tennisvlaanderenid = '" + tennisvlaanderenId + "'";
        ResultSet rs = s.executeQuery(query);

        while (rs.next()) {
          int tennisvlaanderenIdFromDb = rs.getInt("tennisvlaanderenid");
          String naam = rs.getString("naam");
          int punten = rs.getInt("punten");

          gevonden_speler = new Speler(tennisvlaanderenIdFromDb, naam, punten);
        }
          if (gevonden_speler == null) {
            throw new InvalidSpelerException(tennisvlaanderenId + "");
          }

          rs.close();
          s.close();
          connection.commit();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return gevonden_speler;
  }

  @Override
  public List<Speler> getAllSpelers() {
    ArrayList<Speler> resultList = new ArrayList<Speler>();
    try {
        Statement s = (Statement) connection.createStatement();
        String query = "SELECT * FROM speler";
        ResultSet rs = s.executeQuery(query);

        while (rs.next()) {
          int tennisvlaanderenIdFromDb = rs.getInt("tennisvlaanderenid");
          String naam = rs.getString("naam");
          int punten = rs.getInt("punten");

          Speler speler = new Speler(tennisvlaanderenIdFromDb, naam, punten);
          resultList.add(speler);
        }
        rs.close();
        s.close();
        connection.commit();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return resultList;
  }

  @Override
  public void updateSpelerInDb(Speler speler) {
    getSpelerByTennisvlaanderenId(speler.getTennisvlaanderenid());
    try {
        PreparedStatement prepared = (PreparedStatement) connection.prepareStatement("UPDATE speler SET naam = ?, punten = ? WHERE tennisvlaanderenid = ?");
        prepared.setString(1, speler.getNaam());
        prepared.setInt(2, speler.getPunten());
        prepared.setInt(3, speler.getTennisvlaanderenid());
        prepared.executeUpdate();
        prepared.close();
        connection.commit();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  public void deleteSpelerInDb(int tennisvlaanderenid) {
    getSpelerByTennisvlaanderenId(tennisvlaanderenid);
    try {
        PreparedStatement prepared = (PreparedStatement) connection.prepareStatement("DELETE FROM speler WHERE tennisvlaanderenid = ?");
        prepared.setInt(1, tennisvlaanderenid);
        prepared.executeUpdate();
        prepared.close();
        connection.commit();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getHoogsteRankingVanSpeler(int tennisvlaanderenid) {
    getSpelerByTennisvlaanderenId(tennisvlaanderenid);
    String resultString = null;
    try {
        PreparedStatement prepared = (PreparedStatement) connection.prepareStatement("SELECT t.clubnaam, w.finale, w.winnaar " + "FROM wedstrijd w " + "JOIN tornooi t ON w.tornooi = t.id " + "WHERE (w.speler1 = ? OR w.speler2 = ?) " + "ORDER BY w.finale ASC " + "LIMIT 1");
        prepared.setInt(1, tennisvlaanderenid);
        prepared.setInt(2, tennisvlaanderenid);
        ResultSet rs = prepared.executeQuery();

        if (rs.next()) {
          String clubnaam = rs.getString("clubnaam");
          int finale = rs.getInt("finale");
          Integer winnaar = rs.getObject("winnaar") != null ? rs.getInt("winnaar") : null;

          String finaleString;
          if (finale == 1 && winnaar != null && winnaar == tennisvlaanderenid) {
            finaleString = "winst";
          } else if (finale == 1) {
            finaleString = "finale";
          } else if (finale == 2) {
            finaleString = "halve finale";
          } else if (finale == 4) {
            finaleString = "kwart finale";
          } else {
            finaleString = "lager dan kwart finale";
          }
          resultString = "Hoogst geplaatst in het tornooi van " + clubnaam + " met plaats in de " + finaleString;
        } else {
          resultString = "Geen resultaten";
        }
        rs.close();
        prepared.close();
        connection.commit();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return resultString;
  }

  @Override
  public void addSpelerToTornooi(int tornooiId, int tennisvlaanderenId) {
    getSpelerByTennisvlaanderenId(tennisvlaanderenId);
    try {
        PreparedStatement prepared = (PreparedStatement) connection.prepareStatement("INSERT INTO speler_speelt_tornooi (speler, tornooi) VALUES (?, ?);");
        prepared.setInt(1, tennisvlaanderenId);
        prepared.setInt(2, tornooiId);
        prepared.executeUpdate();
        prepared.close();
        connection.commit();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void removeSpelerFromTornooi(int tornooiId, int tennisvlaanderenId) {
    getSpelerByTennisvlaanderenId(tennisvlaanderenId);
    try {
        PreparedStatement prepared = (PreparedStatement) connection.prepareStatement("DELETE FROM speler_speelt_tornooi WHERE speler = ? AND tornooi = ?;");
        prepared.setInt(1, tennisvlaanderenId);
        prepared.setInt(2, tornooiId);
        prepared.executeUpdate();
        prepared.close();
        connection.commit();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
