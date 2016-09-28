package ai.puppet;


class Entry{
	Move _bestMove;
	int _hash;
	float _value;
	int _height;
	boolean _exact;
	boolean _upper;
	Entry(Move bestMove,int hash, float value, int height, boolean exact, boolean upper){
		_bestMove=bestMove;
		_hash=hash;
		_value=value;
		_height=height;
		_exact=exact;
		_upper=upper;
	}
	Entry(){}
};
class TranspositionTable
{
	Entry[] _entries;

	TranspositionTable(int size)
	{
		_entries=new Entry[size];
		for(int i=0;i<size;i++){
			_entries[i]=new Entry();
		}
	}
	void store(PuppetGameState origState, Move bestMove, float value, float alpha, float beta, int height)
	{
		boolean exact,upper;
		if (value <= alpha){
			exact = false;
			upper = true;
		}
		else if (value >= beta){
			exact = false;
			upper = false;
		}
		else{
			exact = true;
			upper = false;
		}
		
		int pos=origState.getHash()%_entries.length;
		_entries[pos]._bestMove=bestMove;
		_entries[pos]._hash=origState.getHash();
		_entries[pos]._value=value;
		_entries[pos]._height=height;
		_entries[pos]._exact=exact;
		_entries[pos]._upper=upper;
	}
	void store(PuppetGameState origState, int depth, Move move, Move bestMove, float value, float alpha, float beta, int height)
	{
		boolean exact,upper;
		if (value <= alpha){
			exact = false;
			upper = true;
		}
		else if (value >= beta){
			exact = false;
			upper = false;
		}
		else{
			exact = true;
			upper = false;
		}
		int pos=origState.getHash(depth, move)%_entries.length;
		_entries[pos]._bestMove=bestMove;
		_entries[pos]._hash=origState.getHash(depth, move);
		_entries[pos]._value=value;
		_entries[pos]._height=height;
		_entries[pos]._exact=exact;
		_entries[pos]._upper=upper;
	}
	Entry lookup(PuppetGameState state, int depth, Move move)
	{
		int hash=state.getHash(depth, move);
		Entry entry=_entries[hash %_entries.length];
		if(entry._hash==hash){
			return entry;
		}else{
			return null;
		}
	}
	Entry lookup(PuppetGameState state)
	{
		Entry entry=_entries[state.getHash() %_entries.length];
		if(entry._hash==state.getHash()){
			return entry;
		}else{
			return null;
		}
	}
};

