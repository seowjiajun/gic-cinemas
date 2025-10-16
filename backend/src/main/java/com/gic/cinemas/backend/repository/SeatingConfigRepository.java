package com.gic.cinemas.backend.repository;

import com.gic.cinemas.backend.model.SeatingConfigEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SeatingConfigRepository extends JpaRepository<SeatingConfigEntity, Long> {

  @Query(
      """
    select sc.id
    from SeatingConfigEntity sc
    where sc.movieTitle = :movieTitle
      and sc.rowCount = :rowCount
      and sc.seatsPerRow = :seatsPerRow
  """)
  Optional<Long> findIdByTitleAndLayout(String movieTitle, int rowCount, int seatsPerRow);

  @Query(
      """
      select sc
      from SeatingConfigEntity sc
      where sc.movieTitle = :movieTitle
        and sc.rowCount = :rowCount
        and sc.seatsPerRow = :seatsPerRow
      """)
  Optional<SeatingConfigEntity> findByTitleAndLayout(
      String movieTitle, int rowCount, int seatsPerRow);
}
