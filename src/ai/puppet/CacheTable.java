package ai.puppet;

import java.util.Random;

import rts.GameState;
import util.Pair;


class PuppetGameState{
	GameState gs;
	int hash;
	static final int MAX_DEPTH=100;
	static final int MAX_CHOICE_POINTS=10;
	static final int MAX_CHOICES=10;
	static int zobrist[][][][]=new int[MAX_DEPTH][2][MAX_CHOICE_POINTS][MAX_CHOICES];
	static{
		Random rng=new Random();
		for(int depth=0;depth<MAX_DEPTH;depth++){
			for(int p=0;p<2;p++){
				for(int point=0;point<MAX_CHOICE_POINTS;point++){
					for(int choice=0;choice<MAX_CHOICES;choice++){
						zobrist[depth][p][point][choice]=rng.nextInt(Integer.MAX_VALUE);
					}
				}
			}
		}
	}
	public PuppetGameState(GameState gs) {
		this.gs=gs.clone();
		Random rng=new Random();
		hash=rng.nextInt(Integer.MAX_VALUE);
	}
	public PuppetGameState(PuppetGameState gs) {
		this.gs=gs.gs;
		hash=gs.hash;
	}
	public PuppetGameState(PuppetGameState oldState, GameState newState, int depth, Move move1, Move move2) {
		this.gs=newState;
		hash=oldState.hash;
		hash=getHash(depth, move1, move2);
	}
	int getHash(int depth, Move move1, Move move2)
	{
		int _hash = hash;
		for (Pair<Integer,Integer> c : move1.choices)
		{
			_hash ^= zobrist[depth][0][c.m_a][c.m_b];
		}
		for (Pair<Integer,Integer> c : move2.choices)
		{
			_hash ^= zobrist[depth+1][1][c.m_a][c.m_b];
		}
		return _hash;
	}
	int getHash(int depth, Move  move)
	{
		int _hash = hash;
		for (Pair<Integer,Integer> c : move.choices)
		{
			_hash ^= zobrist[depth][0][c.m_a][c.m_b];
		}
		return _hash;
	}
	int getHash()  
	{ 
		return hash; 
	}
}
class CacheEntry{
	PuppetGameState _state;
	CacheEntry(PuppetGameState state){
		_state=state;
	}
	CacheEntry(){}
};
class CacheTable
{
	CacheEntry[] _entries;

	CacheTable(int size)
	{
		_entries=new CacheEntry[size];
		for(int i=0;i<size;i++){
			_entries[i]=new CacheEntry();
		}
	}
	void store(PuppetGameState origState, PuppetGameState newState)
	{
		_entries[newState.getHash()%_entries.length]._state=newState;
	}
	CacheEntry lookup(PuppetGameState state, int depth, Move move1, Move move2)
	{
		int hash=state.getHash(depth, move1, move2);
		CacheEntry entry=_entries[hash % _entries.length];
		if(entry._state!=null&&entry._state.getHash()==hash){
			return entry;
		}else{
			return null;
		}
	}
}
