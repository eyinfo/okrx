package io.github.eyinfo.okrx.beans;

import java.io.Serializable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class RequestCacheElement implements Serializable {

    @Id(assignable = true)
    private long id;

    private String cacheKey;

    private String data;

    private long cacheTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
