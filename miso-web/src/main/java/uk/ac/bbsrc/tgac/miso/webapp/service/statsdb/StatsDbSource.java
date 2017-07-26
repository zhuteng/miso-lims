package uk.ac.bbsrc.tgac.miso.webapp.service.statsdb;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import uk.ac.bbsrc.tgac.miso.core.data.Run;
import uk.ac.bbsrc.tgac.miso.core.data.type.PlatformType;
import uk.ac.bbsrc.tgac.miso.webapp.util.RunMetricsSource;

public class StatsDbSource implements RunMetricsSource {
  public static final StatsDbSource INSTANCE = StatsDbSource.create();
  protected static final Logger log = LoggerFactory.getLogger(StatsDbSource.class);

  private static StatsDbSource create() {
    try {
      Context initCtx = new InitialContext();
      Context envCtx = (Context) initCtx.lookup("java:comp/env");
      DataSource source = (DataSource) envCtx.lookup("jdbc/STATSDB");
      if (source != null) {
        return new StatsDbSource(new JdbcTemplate(source));
      }
    } catch (NamingException e) {
      log.warn("Failed to initialise stats DB", e);
    }
    return null;
  }

  private final JdbcTemplate statsDb;

  public StatsDbSource(JdbcTemplate statsDb) {
    super();
    this.statsDb = statsDb;
  }

  @Override
  public String fetchMetrics(Run run) {
    if (run.getPlatformType() != PlatformType.ILLUMINA || run.getId() == Run.UNSAVED_ID
        || run.getSequencerPartitionContainers().size() != 1) {
      return null;
    }

    ObjectMapper mapper = new ObjectMapper();
    ArrayNode output = mapper.createArrayNode();
    PerPositionBaseSequenceQualityReport.INSTANCE.process(run, statsDb, output);
    PerPositionBaseContent.INSTANCE.process(run, statsDb, output);
    try {
      return mapper.writeValueAsString(output);
    } catch (JsonProcessingException e) {
      log.error("Failed to produce stats for " + run.getAlias(), e);
      return null;
    }
  }

}
