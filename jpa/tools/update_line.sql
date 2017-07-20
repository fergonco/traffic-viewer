CREATE OR REPLACE FUNCTION tpg_update_source_line()
  RETURNS void AS
$$
BEGIN
  FOR Loopid  IN 0..162983 LOOP
    begin
      raise notice 'iteration %', Loopid;
      update b1507.shift s set sourcelinecode = (select r.line from app.tpgstoproute r where r.starttpgcode=s.sourcestartpoint and r.endtpgcode=s.sourceendpoint group by r.line) where s.id=Loopid;
    exception when others then
	  raise notice '%', SQLERRM;
    end;
  END LOOP;
RETURN;
END;
$$  LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION tpg_update_source_line_by_count()
  RETURNS void AS
$$
DECLARE
  shift record;
BEGIN
  FOR shift IN select * from b1507.shift where sourcelinecode is null LOOP
    raise notice 'iteration %', shift.id;
    begin
      execute 'update b1507.shift set sourcelinecode = (select line from line_and_numsegments_by_route where starttpgcode=sourcestartpoint and endtpgcode=sourceendpoint and count=osmshiftcount) where id =' || shift.id;
	exception when others then
	  raise notice '%', SQLERRM;
    end;
  END LOOP;
RETURN;
END;
$$  LANGUAGE plpgsql;


