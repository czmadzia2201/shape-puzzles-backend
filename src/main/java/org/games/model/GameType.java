package org.games.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameType {

    @Id
    private String name;

    @OneToMany(mappedBy = "gameType", cascade = CascadeType.ALL)
    private List<Piece> pieces;

    @OneToMany(mappedBy = "gameType", cascade = CascadeType.ALL)
    private List<Task> tasks;

}
