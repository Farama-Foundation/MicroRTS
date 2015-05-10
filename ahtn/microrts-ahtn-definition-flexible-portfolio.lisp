(defdomain microrts-flexible-portfolio
	(

		;; ---- ---- ---- ---- ---- 
		;; ---- OPERATORS	
		;; ---- ---- ---- ---- ---- 

		(:operator (!wait ?time)
			(true)
		)
		(:operator (!wait-for-free-unit ?player)
			(true)
		)
		(:operator (!fill-with-idles ?player)
			(true)
		)
		(:operator (!idle ?unitid)
			(unit ?unitid ?_ ?_ ?_ ?_)
		)
		(:operator (!move ?unitid ?position)
			(and
				(unit ?unitid ?type ?player ?r ?oldposition)
				(can-move ?type)
			)	
		)
		(:operator (!move-into-attack-range ?unitid1 ?unitid2)
			(and
				(unit ?unitid1 ?type1 ?player1 ?r1 ?oldposition1)
				(unit ?unitid2 ?type2 ?player2 ?r2 ?oldposition2)
				(can-move ?type1)
			)	
		)
		(:operator (!move-into-harvest-range ?unitid1 ?unitid2)
			(and
				(unit ?unitid1 ?type1 ?player1 ?r1 ?oldposition1)
				(unit ?unitid2 ?type2 ?player2 ?r2 ?oldposition2)
				(can-move ?type1)
			)	
		)
		(:operator (!move-into-return-range ?unitid1 ?unitid2)
			(and
				(unit ?unitid1 ?type1 ?player1 ?r1 ?oldposition1)
				(unit ?unitid2 ?type2 ?player2 ?r2 ?oldposition2)
				(can-move ?type1)
			)	
		)
		(:operator (!attack ?unitid1 ?unitid2)
			(and
				(unit ?unitid1 ?type1 ?player1 ?r1 ?oldposition1)
				(unit ?unitid2 ?type2 ?player2 ?r2 ?oldposition2)
				(can-attack ?type1)
				(in-attack-range ?unitid1 ?unitid2)
			)	
		)
		(:operator (!harvest ?unitid1 ?unitid2)
			(and
				(unit ?unitid1 ?type1 ?player1 ?r1 ?oldposition1)
				(unit ?unitid2 Resource ?_ ?r2 ?oldposition2)
				(can-harvest ?type1)
				(in-harvest-range ?unitid1 ?unitid2)
			)	
		)
		(:operator (!return ?unitid1 ?unitid2)
			(and
				(unit ?unitid1 ?type1 ?player1 1 ?oldposition1)
				(unit ?unitid2 Base ?_ ?r2 ?oldposition2)
				(can-harvest ?type1)
				(in-return-range ?unitid1 ?unitid2)
			)	
		)
		(:operator (!produce ?unitid1 ?direction ?type)
			(and
				(unit ?unitid1 ?type1 ?player1 ?r1 ?oldposition1)
				(can-produce ?type1 ?type)
				(has-resources-to-produce ?player1 ?type)
				(free-building-position (neighbor-position ?oldposition1 ?direction))
			)	
		)


		;; ---- ---- ---- ---- ---- 
		;; ---- METHODS	
		;; ---- ---- ---- ---- ---- 

		;; Worker rush:
		(:method dp-rush (destroy-player ?player1 ?player2)
			(:method (destroy-player-rush ?player1 ?player2))
		)

		(:method dp-rush-win (destroy-player-rush ?player1 ?player2)
				(:!condition (and
								(not (unit ?_ ?_ ?player2 ?_ ?_))
								(unit ?_ ?_ ?player1 ?_ ?_)
							))
		)
		(:method dp-rush-lose (destroy-player-rush ?player1 ?player2)
				(:!condition (and
								(not (unit ?_ ?_ ?player1 ?_ ?_))
								(unit ?_ ?_ ?player2 ?_ ?_)
							))
		)
		(:method dp-rush-1 (destroy-player-rush ?player1 ?player2)
			(:sequence
				(:!condition (and
								(unit ?_ ?_ ?player2 ?_ ?_)
								(unit ?baseid Base ?player1 ?_ ?_)
								(unit ?resourceid Resource ?_ ?_ ?_)
								(closest-unit-to ?baseid ?workerid Worker ?player1 1 ?_)
							))
				(:method (destroy-player-rush-reserved-unit ?player1 ?player2 ?workerid))
			)
		)
		(:method dp-rush-2 (destroy-player-rush ?player1 ?player2)
			(:sequence
				(:!condition (and
								(unit ?_ ?_ ?player2 ?_ ?_)
								(unit ?baseid Base ?player1 ?_ ?_)
								(closest-unit-to ?baseid ?resourceid Resource ?_ ?_ ?_)								
								(not (unit ?_ Worker ?player1 1 ?_))
								(closest-unit-to ?resourceid ?workerid Worker ?player1 0 ?_)
							))
				(:method (destroy-player-rush-reserved-unit ?player1 ?player2 ?workerid))
			)
		)
		(:method dp-rush-3 (destroy-player-rush ?player1 ?player2)
			(:sequence
				(:!condition (and
								(unit ?_ ?_ ?player2 ?_ ?_)
								(unit ?_ ?_ ?player1 ?_ ?_)
								(or
									(not (unit ?workerid Worker ?player1 ?_ ?_))
									(not (unit ?baseid Base ?player1 ?_ ?_))
									(not (unit ?resourceid Resource ?_ ?_ ?_))
								)
							))
				(:method (destroy-player-rush-reserved-unit ?player1 ?player2 -1))
			)
		)

		(:method dprru-win (destroy-player-rush-reserved-unit ?player1 ?player2 ?reservedunitid)
			(:!condition (and
							(unit ?_ ?_ ?player1 ?_ ?_)
							(not (unit ?_ ?_ ?player2 ?_ ?_))
						))
		)
		(:method dprru-lose (destroy-player-rush-reserved-unit ?player1 ?player2 ?reservedunitid)
			(:!condition (and
							(unit ?_ ?_ ?player2 ?_ ?_)
							(not (unit ?_ ?_ ?player1 ?_ ?_))
						))
		)
		(:method dprru-reservedkilled (destroy-player-rush-reserved-unit ?player1 ?player2 ?reservedunitid)
			(:sequence
				(:!condition (and (not (= ?reservedunitid -1)) 
								  (not (unit ?reservedunitid ?_ ?_ ?_ ?_))))
				(:method (destroy-player-rush ?player1 ?player2))
			)
		)
		(:method dprru-reservedok (destroy-player-rush-reserved-unit ?player1 ?player2 ?reservedunitid)
			(:sequence
				(:!condition (or (= ?reservedunitid -1) 
								 (unit ?reservedunitid ?_ ?_ ?_ ?_)))
				(:method (destroy-player-rush-reserved-unit-rounds ?player1 ?player2 ?reservedunitid -1))
			)
		)

		(:method dprru-nextunit (destroy-player-rush-reserved-unit-rounds ?player1 ?player2 ?reservedunitid ?lastunit)
			(:sequence
				(:!condition (next-available-unit ?lastunit ?player1 ?unitid))
				(:parallel
					(:method (worker-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid))
					(:method (destroy-player-rush-reserved-unit-rounds ?player1 ?player2 ?reservedunitid ?unitid))
				)
			)
		)
		(:method dprru-nomoreunits (destroy-player-rush-reserved-unit-rounds ?player1 ?player2 ?reservedunitid ?lastunit)
			(:sequence
				(:!condition (no-more-available-units ?lastunit ?player1))
				(:operator (!fill-with-idles ?player1))
				(:operator (!wait-for-free-unit ?player1))
				;; (:operator (!wait 4))
				(:method (destroy-player-rush-reserved-unit ?player1 ?player2 ?reservedunitid))
			)
		)

		(:method wrub-reserved-1 (worker-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and 
								(= ?unitid ?reservedunitid)
								(unit ?unitid ?_ ?_ 0 ?_)
								(closest-unit-to ?unitid ?resourceid Resource ?_ ?_ ?_)
								(not (in-harvest-range ?unitid ?resourceid))
							))
				(:operator (!move-into-harvest-range ?unitid ?resourceid))
			)
		)
		(:method wrub-reserved-2 (worker-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and 
								(= ?unitid ?reservedunitid)
								(unit ?unitid ?_ ?_ 0 ?_)
								(closest-unit-to ?unitid ?resourceid Resource ?_ ?_ ?_)
								(in-harvest-range ?unitid ?resourceid)
							))
				(:operator (!harvest ?unitid ?resourceid))
			)
		)
		(:method wrub-reserved-3 (worker-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and 
								(= ?unitid ?reservedunitid)
								(unit ?unitid ?_ ?_ 1 ?_)
								(closest-unit-to ?unitid ?baseid Base ?player1 ?_ ?_)
								(not (in-return-range ?unitid ?baseid))
							))
				(:operator (!move-into-return-range ?unitid ?baseid))
			)
		)
		(:method wrub-reserved-4 (worker-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and 
								(= ?unitid ?reservedunitid)
								(unit ?unitid ?_ ?_ 1 ?_)
								(closest-unit-to ?unitid ?baseid Base ?player1 ?_ ?_)
								(in-return-range ?unitid ?baseid)
							))
				(:operator (!return ?unitid ?baseid))
			)
		)
		(:method wrub-reserved-5 (worker-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and 
								(= ?unitid ?reservedunitid)
								(unit ?unitid ?_ ?_ 1 ?_)
								(not (closest-unit-to ?unitid ?baseid Base ?player1 ?_ ?_))
							))
				(:operator (!idle ?unitid))
			)
		)

		(:method wrub-base-produce (worker-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and
								(not (= ?unitid ?reservedunitid))
								(unit ?unitid Base ?_ ?_ ?_)
								(has-resources-to-produce ?player1 Worker)
								(free-producing-direction ?unitid ?direction)
							))
				(:operator (!produce ?unitid ?direction Worker))
			)
		)
		(:method wrub-base-nothing (worker-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and
								(not (= ?unitid ?reservedunitid))
								(unit ?unitid Base ?_ ?_ ?_)
								(or 
									(not (has-resources-to-produce ?player1 Worker))
									(not (free-producing-direction ?unitid ?direction))
								)
							))
			)
		)
		(:method wrub-barracks (worker-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and
								(not (= ?unitid ?reservedunitid))
								(unit ?unitid Barracks ?_ ?_ ?_)
							))
			)
		)
		(:method wrub-melee-attack (worker-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:condition (and
								(not (= ?unitid ?reservedunitid))
								(unit ?unitid ?type ?_ ?_ ?_)
								(can-attack ?type)
								(unit ?enemyid ?_ ?player2 ?_ ?_)
								(in-attack-range ?unitid ?enemyid)
							))
				(:operator (!attack ?unitid ?enemyid))
			)
		)
		(:method wrub-melee-move (worker-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:condition (and
								(not (= ?unitid ?reservedunitid))
								(unit ?unitid ?type ?_ ?_ ?_)
								(can-attack ?type)
								(unit ?enemyid ?_ ?player2 ?_ ?_)
								(not (in-attack-range ?unitid ?enemyid))
								(path-to-attack ?unitid ?enemyid)
							))
				(:operator (!move-into-attack-range ?unitid ?enemyid))
			)
		)
		(:method wrub-melee-cantmove (worker-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and (not (= ?unitid ?reservedunitid))
								  (unit ?unitid ?type ?_ ?_ ?_)
								  (can-attack ?type)
							 ))
				(:!condition (not (and
								(unit ?enemyid ?_ ?player2 ?_ ?_)
								(path-to-attack ?unitid ?enemyid)
							)))
			)
		)

		;; Light rush:
		(:method dp-light-rush (destroy-player ?player1 ?player2)
			(:method (destroy-player-light-rush ?player1 ?player2))
		)

		(:method dp-light-rush-win (destroy-player-light-rush ?player1 ?player2)
				(:!condition (and
								(not (unit ?_ ?_ ?player2 ?_ ?_))
								(unit ?_ ?_ ?player1 ?_ ?_)
							))
		)
		(:method dp-light-rush-lose (destroy-player-light-rush ?player1 ?player2)
				(:!condition (and
								(not (unit ?_ ?_ ?player1 ?_ ?_))
								(unit ?_ ?_ ?player2 ?_ ?_)
							))
		)
		(:method dp-light-rush-1 (destroy-player-light-rush ?player1 ?player2)
			(:sequence
				(:!condition (and
								(unit ?_ ?_ ?player2 ?_ ?_)
								(unit ?baseid Base ?player1 ?_ ?_)
								(unit ?resourceid Resource ?_ ?_ ?_)
								(closest-unit-to ?baseid ?workerid Worker ?player1 1 ?_)
							))
				(:method (destroy-player-light-rush-reserved-unit ?player1 ?player2 ?workerid))
			)
		)
		(:method dp-light-rush-2 (destroy-player-light-rush ?player1 ?player2)
			(:sequence
				(:!condition (and
								(unit ?_ ?_ ?player2 ?_ ?_)
								(unit ?baseid Base ?player1 ?_ ?_)
								(closest-unit-to ?baseid ?resourceid Resource ?_ ?_ ?_)								
								(not (unit ?_ Worker ?player1 1 ?_))
								(closest-unit-to ?resourceid ?workerid Worker ?player1 0 ?_)
							))
				(:method (destroy-player-light-rush-reserved-unit ?player1 ?player2 ?workerid))
			)
		)
		(:method dp-light-rush-3 (destroy-player-light-rush ?player1 ?player2)
			(:sequence
				(:!condition (and
								(unit ?_ ?_ ?player2 ?_ ?_)
								(unit ?_ ?_ ?player1 ?_ ?_)
								(or
									(not (unit ?workerid Worker ?player1 ?_ ?_))
									(not (unit ?baseid Base ?player1 ?_ ?_))
									(not (unit ?resourceid Resource ?_ ?_ ?_))
								)
							))
				(:method (destroy-player-light-rush-reserved-unit ?player1 ?player2 -1))
			)
		)

		(:method dprlru-win (destroy-player-light-rush-reserved-unit ?player1 ?player2 ?reservedunitid)
			(:!condition (and
							(unit ?_ ?_ ?player1 ?_ ?_)
							(not (unit ?_ ?_ ?player2 ?_ ?_))
						))
		)
		(:method dprlru-lose (destroy-player-light-rush-reserved-unit ?player1 ?player2 ?reservedunitid)
			(:!condition (and
							(unit ?_ ?_ ?player2 ?_ ?_)
							(not (unit ?_ ?_ ?player1 ?_ ?_))
						))
		)
		(:method dprlru-reservedkilled (destroy-player-light-rush-reserved-unit ?player1 ?player2 ?reservedunitid)
			(:sequence
				(:!condition (and (not (= ?reservedunitid -1)) 
								  (not (unit ?reservedunitid ?_ ?_ ?_ ?_))))
				(:method (destroy-player-rush ?player1 ?player2))
			)
		)
		(:method dprlru-reservedok (destroy-player-light-rush-reserved-unit ?player1 ?player2 ?reservedunitid)
			(:sequence
				(:!condition (or (= ?reservedunitid -1) 
								 (unit ?reservedunitid ?_ ?_ ?_ ?_)))
				(:method (destroy-player-light-rush-reserved-unit-rounds ?player1 ?player2 ?reservedunitid -1))
			)
		)

		(:method dprlru-nextunit (destroy-player-light-rush-reserved-unit-rounds ?player1 ?player2 ?reservedunitid ?lastunit)
			(:sequence
				(:!condition (next-available-unit ?lastunit ?player1 ?unitid))
				(:parallel
					(:method (light-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid))
					(:method (destroy-player-light-rush-reserved-unit-rounds ?player1 ?player2 ?reservedunitid ?unitid))
				)
			)
		)
		(:method dprlru-nomoreunits (destroy-player-light-rush-reserved-unit-rounds ?player1 ?player2 ?reservedunitid ?lastunit)
			(:sequence
				(:!condition (no-more-available-units ?lastunit ?player1))
				(:operator (!fill-with-idles ?player1))
				(:operator (!wait-for-free-unit ?player1))
				;; (:operator (!wait 4))
				(:method (destroy-player-light-rush-reserved-unit ?player1 ?player2 ?reservedunitid))
			)
		)

		(:method lrub-reserved-1 (light-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and 
								(= ?unitid ?reservedunitid)
								(unit ?unitid ?_ ?_ 0 ?_)
								(not (unit ?_ Barracks ?player1 ?_ ?_))
								(has-resources-to-produce ?player1 Barracks)
								(free-producing-direction ?unitid ?direction)
							))
				(:operator (!produce ?unitid ?direction Barracks))
			)
		)
		(:method lrub-reserved-2 (light-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and 
								(= ?unitid ?reservedunitid)
								(unit ?unitid ?_ ?_ 0 ?_)
								(or
									(unit ?_ Barracks ?player1 ?_ ?_)
									(not (has-resources-to-produce ?player1 Barracks))
									(not (free-producing-direction ?unitid ?direction))
									)
								(closest-unit-to ?unitid ?resourceid Resource ?_ ?_ ?_)
								(not (in-harvest-range ?unitid ?resourceid))
							))
				(:operator (!move-into-harvest-range ?unitid ?resourceid))
			)
		)
		(:method lrub-reserved-3 (light-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and 
								(= ?unitid ?reservedunitid)
								(unit ?unitid ?_ ?_ 0 ?_)
								(or
									(unit ?_ Barracks ?player1 ?_ ?_)
									(not (has-resources-to-produce ?player1 Barracks))
									(not (free-producing-direction ?unitid ?direction))
									)
								(closest-unit-to ?unitid ?resourceid Resource ?_ ?_ ?_)
								(in-harvest-range ?unitid ?resourceid)
							))
				(:operator (!harvest ?unitid ?resourceid))
			)
		)
		(:method lrub-reserved-4 (light-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and 
								(= ?unitid ?reservedunitid)
								(unit ?unitid ?_ ?_ 1 ?_)
								(closest-unit-to ?unitid ?baseid Base ?player1 ?_ ?_)
								(not (in-return-range ?unitid ?baseid))
							))
				(:operator (!move-into-return-range ?unitid ?baseid))
			)
		)
		(:method lrub-reserved-5 (light-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and 
								(= ?unitid ?reservedunitid)
								(unit ?unitid ?_ ?_ 1 ?_)
								(closest-unit-to ?unitid ?baseid Base ?player1 ?_ ?_)
								(in-return-range ?unitid ?baseid)
							))
				(:operator (!return ?unitid ?baseid))
			)
		)
		(:method lrub-reserved-6 (light-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and 
								(= ?unitid ?reservedunitid)
								(unit ?unitid ?_ ?_ 1 ?_)
								(not (closest-unit-to ?unitid ?baseid Base ?player1 ?_ ?_))
							))
				(:operator (!idle ?unitid))
			)
		)

		(:method lrub-base-produce (light-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and
								(not (= ?unitid ?reservedunitid))
								(unit ?unitid Base ?_ ?_ ?_)
								(not (unit ?_ Worker ?player1 ?_ ?_))
								(has-resources-to-produce ?player1 Worker)
								(free-producing-direction ?unitid ?direction)
							))
				(:operator (!produce ?unitid ?direction Worker))
			)
		)
		(:method lrub-base-nothing (light-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and
								(not (= ?unitid ?reservedunitid))
								(unit ?unitid Base ?_ ?_ ?_)
								(or 
									(unit ?_ Worker ?player1 ?_ ?_)
									(not (has-resources-to-produce ?player1 Worker))
									(not (free-producing-direction ?unitid ?direction))
								)
							))
			)
		)
		(:method lrub-barracks-produce (light-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and
								(not (= ?unitid ?reservedunitid))
								(unit ?unitid Barracks ?_ ?_ ?_)
								(has-resources-to-produce ?player1 Light)
								(free-producing-direction ?unitid ?direction)
							))
				(:operator (!produce ?unitid ?direction Light))
			)
		)
		(:method lrub-barracks-produce (light-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and
								(not (= ?unitid ?reservedunitid))
								(unit ?unitid Barracks ?_ ?_ ?_)
								(has-resources-to-produce ?player1 Ranged)
								(free-producing-direction ?unitid ?direction)
							))
				(:operator (!produce ?unitid ?direction Ranged))
			)
		)
		(:method lrub-barracks-nothing (light-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and
								(not (= ?unitid ?reservedunitid))
								(unit ?unitid Barracks ?_ ?_ ?_)
								(or
									(not (has-resources-to-produce ?player1 Light))
									(not (free-producing-direction ?unitid ?direction))
								)
							))
			)
		)
		(:method lrub-melee-attack (light-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:condition (and
								(not (= ?unitid ?reservedunitid))
								(unit ?unitid ?type ?_ ?_ ?_)
								(can-attack ?type)
								(unit ?enemyid ?_ ?player2 ?_ ?_)
								(in-attack-range ?unitid ?enemyid)
							))
				(:operator (!attack ?unitid ?enemyid))
			)
		)
		(:method lrub-melee-move (light-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:condition (and
								(not (= ?unitid ?reservedunitid))
								(unit ?unitid ?type ?_ ?_ ?_)
								(can-attack ?type)
								(unit ?enemyid ?_ ?player2 ?_ ?_)
								(not (in-attack-range ?unitid ?enemyid))
								(path-to-attack ?unitid ?enemyid)
							))
				(:operator (!move-into-attack-range ?unitid ?enemyid))
			)
		)
		(:method lrub-melee-cantmove (light-rush-unit-behavior ?player1 ?player2 ?reservedunitid ?unitid)
			(:sequence
				(:!condition (and (not (= ?unitid ?reservedunitid))
								  (unit ?unitid ?type ?_ ?_ ?_)
    							  (can-attack ?type)
							 ))
				(:!condition (not (and
								(unit ?enemyid ?_ ?player2 ?_ ?_)
								(path-to-attack ?unitid ?enemyid)
							)))
			)
		)
	)
)
