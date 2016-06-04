package redis.clients.johm.issues;

import redis.clients.johm.Model;

import javax.persistence.*;
import java.io.Serializable;

@redis.clients.johm.Model
@Entity
@Table(name="item")
public class Issue9Item implements Serializable {

	@redis.clients.johm.Id
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;

	public Issue9Item(){
	}

	public Issue9Item(long id){
		this.id = id;
	}

	public long getMId() {
		return id;
	}
}
