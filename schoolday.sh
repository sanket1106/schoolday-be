ENV=$1 # local
ACTION=$2 # init, start, stop, rm, create-schema, seed-data
ENTITY=${3:-all}  # Entity: mysql, app, all (default to 'all' if not provided)

NETWORK_NAME="${ENV}-schoolday-net"
MYSQL_NAME="mysql8"

# Ensure ENV, ACTION, ENTITY are provided
if [[ -z "$ENV" || -z "$ACTION" || -z "$ENTITY" ]]; then
  echo "Usage: $0 <ENV> <ACTION> <ENTITY>"
  echo "Example: $0 dev create all"
  exit 1
fi

init_network() {
  ./infra/containers/init_network.sh $NETWORK_NAME
}

init_mysql() {
	./infra/containers/init_mysql.sh $MYSQL_NAME $NETWORK_NAME
}

create_schema() {
  ./infra/db/create_schema.sh
}

seed_data() {
  ./infra/db/seed_data.sh
}

do_action() {
  local target=$1
  local action=$2

  case "$action" in
    start|stop|rm)
      if podman container exists "$target"; then
        echo "${action}ing container: $target"
        podman "$action" "$target"
      else
        echo "Container '$target' does not exist."
      fi
      ;;
    *)
      echo "Unsupported action: $action"
      ;;
  esac
}

case "$ACTION" in
	init)
		init_network
		[[ $ENTITY == "mysql" || $ENTITY == "all" ]] && init_mysql
		;;

	create)
	  [[ $ENTITY == "mysql-schema" ]] && create_schema
	  ;;

	insert)
	  [[ $ENTITY == "data" ]] && seed_data
	  ;;

	start|stop|rm)
    	[[ $ENTITY == "mysql" || $ENTITY == "all" ]] && do_action "$MYSQL_NAME" "$ACTION"
    	;;

	*)
    	echo "Unknown action: $ACTION"
    	exit 1
    	;;
esac
