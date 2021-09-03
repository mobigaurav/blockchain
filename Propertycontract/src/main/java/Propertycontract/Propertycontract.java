package Propertycontract;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import com.owlike.genson.Genson;


@Contract(name = "Propertycontract", info = @Info(title = "Propertycontract contract", description = "A Sample Property transfer chaincode example", version = "0.0.1-SNAPSHOT"))
@Default
public class Propertycontract implements ContractInterface {
	private final Genson genson = new Genson();

	private enum PropertyErrors {
	   Property_NOT_FOUND, Property_ALREADY_EXISTS
	}
	
	/**
	 * Add some initial properties to the ledger
	 *
	 * @param ctx the transaction context
	 */
	@Transaction()
	public void initLedger(final Context ctx) {

		ChaincodeStub stub = ctx.getStub();

		Propertymodel property = new Propertymodel("1", "1990", "Gaurav", "$10000");

		String PropertyState = genson.serialize(property);

		stub.putStringState("1", PropertyState);
	}
	
	/**
	 * Add new Property on the ledger.
	 *
	 * @param ctx       the transaction context
	 * @param id        the key for the new Property
	 * @param model     the model of the new Property
	 * @param ownername the owner of the new Property
	 * @param value     the value of the new Property
	 * @return the created Property
	 */

	@Transaction()
	public Propertymodel addNewProperty(final Context ctx, final String id, final String model, final String ownername,
			final String value) {

		ChaincodeStub stub = ctx.getStub();

		String PropertyState = stub.getStringState(id);

		if (!PropertyState.isEmpty()) {
			String errorMessage = String.format("Property %s already exists", id);
			System.out.println(errorMessage);
			throw new ChaincodeException(errorMessage, PropertyErrors.Property_ALREADY_EXISTS.toString());
		}

		Propertymodel Property = new Propertymodel(id, model, ownername, value);

		PropertyState = genson.serialize(Property);

		stub.putStringState(id, PropertyState);

		return Property;
	}

	/**
	 * Retrieves a Property based upon Property Id from the ledger.
	 *
	 * @param ctx the transaction context
	 * @param id  the key
	 * @return the Property found on the ledger if there was one
	 */
	@Transaction()
	public Propertymodel queryPropertyById(final Context ctx, final String id) {
		ChaincodeStub stub = ctx.getStub();
		String PropertyState = stub.getStringState(id);

		if (PropertyState.isEmpty()) {
			String errorMessage = String.format("Property %s does not exist", id);
			System.out.println(errorMessage);
			throw new ChaincodeException(errorMessage, PropertyErrors.Property_NOT_FOUND.toString());
		}

		Propertymodel Property = genson.deserialize(PropertyState, Propertymodel.class);
		return Property;
	}

	/**
	 * Changes the owner of a Property on the ledger.
	 *
	 * @param ctx      the transaction context
	 * @param id       the key
	 * @param newOwner the new owner
	 * @return the updated Property
	 */
	@Transaction()
	public Propertymodel changePropertyOwnership(final Context ctx, final String id, final String newPropertyOwner) {
		ChaincodeStub stub = ctx.getStub();

		String PropertyState = stub.getStringState(id);

		if (PropertyState.isEmpty()) {
			String errorMessage = String.format("Property %s does not exist", id);
			System.out.println(errorMessage);
			throw new ChaincodeException(errorMessage, PropertyErrors.Property_NOT_FOUND.toString());
		}

		Propertymodel Property = genson.deserialize(PropertyState, Propertymodel.class);

		Propertymodel newProperty = new Propertymodel(Property.getId(), Property.getModel(), newPropertyOwner, Property.getValue());

		String newPropertyState = genson.serialize(newProperty);

		stub.putStringState(id, newPropertyState);

		return newProperty;
	}

}
