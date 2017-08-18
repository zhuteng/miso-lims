/*
 * Copyright (c) 2012. The Genome Analysis Centre, Norwich, UK
 * MISO project contacts: Robert Davey @ TGAC
 * *********************************************************************
 *
 * This file is part of MISO.
 *
 * MISO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MISO is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MISO.  If not, see <http://www.gnu.org/licenses/>.
 *
 * *********************************************************************
 */

package uk.ac.bbsrc.tgac.miso.core.util;

import uk.ac.bbsrc.tgac.miso.core.data.*;
import uk.ac.bbsrc.tgac.miso.core.data.impl.view.PoolableElementView;
import uk.ac.bbsrc.tgac.miso.core.data.type.PlatformType;

/**
 * Utility class to build sample sheets from run information
 * 
 * @author Rob Davey
 * @date 17/08/17
 * @since 0.2.73
 */
public class RunProcessingUtils {
  public static String getSampleSheetByPlatform(Run r, SequencerPartitionContainer f, String platform, String type, String userName) {
    StringBuilder sb = new StringBuilder();
    if (PlatformType.ILLUMINA.getKey().equals(platform) && !"IEM".equals(type)) {
      // build casava samplesheet
      return buildIlluminaDemultiplexCSV(r, f, type, userName);
    }
    //Lane,SampleID,Sample_Name,Sample_Plate,Sample_Well,i7_index_ID,Index,i5_index_ID,Index2,Sample_Project,Description,Library,TaxID
    else if (PlatformType.ILLUMINA.getKey().equals(platform) && "IEM".equals(type)) {
      sb.append("[Data]").append("\n");
      sb.append("Lane,").append("Sample_ID,").append("Sample_Name,").append("Sample_Plate,").append("Sample_Well,")
              .append("i7_index_ID,").append("Index1_Sequence,").append("i5_index_ID,").append("Index2_Sequence,")
              .append("Sample_Project_ID,").append("Description,").append("Library_ID,").append("Sample_Scientific_Name,")
              .append("NCBI_Taxon_ID")
              .append("\n");
      for (Partition l : f.getPartitions()) {
        Pool p = l.getPool();
        if (p != null) {
          for (PoolableElementView ld : p.getPoolableElementViews()) {
            sb.append(l.getPartitionNumber()).append(",")
                    .append(ld.getSampleId()).append(",")
                    .append(ld.getSampleAlias()).append(",")
                    .append(p.getBox() != null ? p.getBox().getId() : "").append(",")
                    .append(p.getBox() != null ? p.getBoxPosition() : "").append(",");

            if (ld.getIndices() != null && !ld.getIndices().isEmpty()) {
              if (ld.getIndices().size() == 2) {
                for (Index index : ld.getIndices()) {
                  sb.append(index.getName())
                          .append(",")
                          .append(index.getSequence());
                  sb.append(",");
                }
              }
              else {
                Index index = ld.getIndices().get(0);
                sb.append(index.getName())
                        .append(",")
                        .append(index.getSequence())
                        .append(",,,");
              }
            } else {
              sb.append(",,,,");
            }

            sb.append(ld.getProjectId()).append(",")
                    .append(ld.getProjectAlias()).append(",")
                    .append(ld.getLibraryId()).append(",")
                    .append(ld.getSampleScientificName()).append(",")
                    .append(ld.getSampleTaxonIdentifier())
                    .append("\n");
          }
        }
      }
    }
    return sb.toString();
  }

  public static String buildIlluminaDemultiplexCSV(Run r, SequencerPartitionContainer f, String casavaVersion,
      String userName) {
    boolean newCasava = false;

    StringBuilder sb = new StringBuilder();
    sb.append("FCID,").append("Lane,").append("SampleID,").append("SampleRef,").append("Index,").append("Description,").append("Control,")
        .append("Recipe,").append("Operator");

    if (casavaVersion.compareTo("1.7") >= 0) {
      newCasava = true;
    }

    if (newCasava) {
      sb.append(",Project\n");
    } else {
      sb.append("\n");
    }

    for (Partition l : f.getPartitions()) {
      Pool p = l.getPool();
      if (p != null) {
        for (PoolableElementView ld : p.getPoolableElementViews()) {
          sb.append(f.getIdentificationBarcode()).append(",").append(l.getPartitionNumber()).append(",").append(f.getId()).append("_")
              .append(ld.getLibraryName()).append("_").append(ld.getDilutionName()).append(",")
              .append(ld.getSampleAlias().replaceAll("\\s", "")).append(",");

          if (ld.getIndices() != null && !ld.getIndices().isEmpty()) {
            boolean first = true;
            for (Index index : ld.getIndices()) {
              sb.append(index.getSequence());
              if (first) {
                first = false;
              } else {
                sb.append("-");
              }
            }
            sb.append(",");
          } else {
            sb.append(",");
          }

          sb.append(ld.getLibraryDescription()).append(",").append("N").append(",").append("NA").append(",").append(userName);

          if (newCasava) {
            sb.append(",").append(ld.getProjectAlias().replaceAll("\\s", "")).append("\n");
          } else {
            sb.append("\n");
          }
        }
      }
    }
    return sb.toString();
  }
}
