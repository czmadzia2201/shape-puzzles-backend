package org.games.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Set;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameType {

    @Id
    private String name;

    private String displayName;

    @OneToMany(mappedBy = "gameType", cascade = CascadeType.ALL)
    private Set<Piece> pieces;

    @OneToMany(mappedBy = "gameType", cascade = CascadeType.ALL)
    private Set<Task> tasks;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Point> baseShape;

    private double unitSize;

}
